#include <iostream>
#if defined(__WIN32__) || defined(_MSC_VER)
	#include <winsock2.h>
#elif defined(__unix__) || defined(__linux__)
	#include <sys/types.h>
	#include <sys/socket.h>
	#include <netinet/in.h>
	#include <arpa/inet.h>
	#define INVALID_SOCKET -1
	#define SOCKET_ERROR -1
#endif
#include <fstream>
#include <sstream>
#include <string.h>
#include <cstdlib>
#include <limits>
#include "commandline.h"
#include "logger.h"

using namespace std;

bool checkExistFile(string file) {
	ifstream input;
	input.open(file.c_str(), ifstream::in);
	input.close();
	if(input.fail()) {
		input.clear(ios::failbit);
		return false;
	}
	return true;
}

/*
 * Reads out the port of bh
 */
int readPortFromFile(string filename, Logger &logger) {
	int port = -1;

	//Create line buffer
	char line[1024];

	//Create fileinputstream
	ifstream fileInputStream;

	//Open the stream
	fileInputStream.open(filename.c_str(), ios::in);

	if (fileInputStream.good()) {
		logger.debug("File openend");

		//Seek to beginning of the file
		fileInputStream.seekg(0L, ios::beg);

		//While not end of file
    	while (!fileInputStream.eof()) {
    		//Read file line by line
    		fileInputStream.getline(line, 1024);
    		logger << "Line read: " << line << "\n";
    		stringstream sstr(line);
    		sstr >> port;
			break;
    	}

    	//Close the stream
    	fileInputStream.close();
    	logger << "File closed" << "\n";
	} else {
		logger << "File could not be openend" << "\n";
	}
	if ( (port > 0) && (port < 65536) ) {
		return port;
	}
	return -1;
}

int sendData(int socket, const char* data, Logger &logger) {
	//Create the buffer
	char buf[256];

	//Copy data to buffer and send the data
	strcpy(buf, data);
	int rc = send(socket, buf, strlen(data), 0);
	if (rc == -1) {
		#if defined(__WIN32__) || defined(_MSC_VER)
		logger << "Error: sendData Failed: '" << data << "', Error-Code: " << WSAGetLastError() << "\n";
		#elif defined(__unix__) || defined(__linux__)
		logger << "Error: sendData Failed: '" << data << "'" << "\n";
		perror("");
		#endif
		return 1;
	} else {
		logger << "Data sent: " << data << "\n";
	}
	return 0;
}

int main(int argc, char *argv[]) {
	string logPath = argv[0];
	string logFile = logPath + "debug.txt";

	Logger logger(logFile, true, false);

	string forceDebugFile = logPath + "forceDebug.txt";
	logger << "Check if ForceDebug-File exist: " << forceDebugFile << ": ";
	if (checkExistFile(forceDebugFile)) {
		logger << "Found, debug to file enabled\n";
		logger.setWriteToFile(true);
	} else {
		logger << "Not found, debug to file disabled\n";
	}

	CommandLine commandLine(argc, argv, logger);

	//check parameters
	if (commandLine.isParamsOK() == false) {
		string usage = "Usage:\nBHTransmit parameter value\n\nParameters:\n-f\tFile containing Links (and Thumbnail-URLs)\n-u\tURL to a website which contains Links\n-i\tURL to website which contains embedded images\n-d\tTextfile for Debug-Messages\n\nExample:\nBHTransmit -u http://www.google.ch/\n";
		logger << usage;
		return 1;
	}

	//Get the path to the userprofile
	#if defined(__WIN32__) || defined(_MSC_VER)
	char *userProfilePath = getenv("userprofile");
	#elif defined(__unix__) || defined(__linux__)
	char *userProfilePath = getenv("HOME");
	#endif
	if (userProfilePath == NULL) {
		logger << "Error: Homepath not found\n";
		return 1;
	}

	/*
	 * Read out the port.txt-File created by BilderHerunterlader which
	 * contains the current port to send the data
	 */
	string portFilename;
	portFilename = portFilename.insert(0, userProfilePath);
	#if defined(__WIN32__) || defined(_MSC_VER)
	portFilename += "\\.BH\\port.txt";
	#elif defined(__unix__) || defined(__linux__)
	portFilename += "/.BH/port.txt";
	#endif
	logger << portFilename << "\n";
	int port = readPortFromFile(portFilename, logger);
	if (port == -1) {
		logger << "Error: Port not found" << "\n";
		return 1;
	} else {
		logger << "Port found: " << port << "\n";
	}

	#if defined(__WIN32__) || defined(_MSC_VER)
	//Startup WSA
	WSAData wsa;
	int rc = WSAStartup(MAKEWORD(2, 0), &wsa);
	if (rc != 0) {
		logger << "Error: startWinsock, Error-Code: " << rc << "\n";
		#if defined(__WIN32__) || defined(_MSC_VER)
		WSACleanup();
		#endif
		return 1;
	}
	logger << "WSA started" << "\n";
	#elif defined(__unix__) || defined(__linux__)
	int rc;
	#endif

	//Create Socket
	#if defined(__WIN32__) || defined(_MSC_VER)
	SOCKET s;
	#elif defined(__unix__) || defined(__linux__)
	int s;
	#endif
	s = socket(AF_INET, SOCK_STREAM, 0);
	if (s == INVALID_SOCKET) {
		logger << "Error: Invalid Socket" << "\n";
		#if defined(__WIN32__) || defined(_MSC_VER)
		WSACleanup();
		#endif
		return 1;
	}
	logger << "Socket created" << "\n";

	//Connect
	#if defined(__WIN32__) || defined(_MSC_VER)
	SOCKADDR_IN addr;
	memset(&addr, 0, sizeof(SOCKADDR_IN));
	#elif defined(__unix__) || defined(__linux__)
	struct sockaddr_in addr;
	#endif
	addr.sin_family = AF_INET;
	addr.sin_port = htons(port);
	addr.sin_addr.s_addr = inet_addr("127.0.0.1");

	#if defined(__WIN32__) || defined(_MSC_VER)
	rc = connect(s, (SOCKADDR*)&addr, sizeof(SOCKADDR));
	#elif defined(__unix__) || defined(__linux__)
	rc = connect(s, (struct sockaddr *)&addr, sizeof(addr));
	#endif
	if (rc == SOCKET_ERROR) {
		#if defined(__WIN32__) || defined(_MSC_VER)
		logger << "Error: Could not connect, Error-Code: " << WSAGetLastError() << "\n";
		WSACleanup();
		#elif defined(__unix__) || defined(__linux__)
		perror("Error: Could not connect, Error-Code: ");
		#endif
		return 1;
	}
	logger << "Connection established" << "\n";


	//Send the data
	sendData(s, "SOF\n", logger);

	string dataToSend = "";
	if (commandLine.getParamFile().length() > 0) {
		dataToSend = commandLine.getParamFile();
	} else if (commandLine.getParamUrl().length() > 0) {
		dataToSend = "URL:" + commandLine.getParamUrl();
	} else if (commandLine.getParamImg().length() > 0) {
		dataToSend = "IMG:" + commandLine.getParamImg();
	}
	dataToSend += "\n";
	sendData(s, dataToSend.c_str(), logger);


	sendData(s, "EOF", logger);

	//Close the socket
	#if defined(__WIN32__) || defined(_MSC_VER)
	closesocket(s);
	WSACleanup();
	#elif defined(__unix__) || defined(__linux__)
	close(s);
	#endif

	logger << "Connection closed" << "\n";

	//Exit
	return EXIT_SUCCESS;
}
