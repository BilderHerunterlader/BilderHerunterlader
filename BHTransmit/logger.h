/*
 * logger.h
 *
 *  Created on: 02.03.2010
 *      Author: Administrator
 */

#ifndef LOGGER_H_
#define LOGGER_H_

#include <fstream>
#include <iostream>

class Logger {
	private:
		std::string logFile;
		bool writeToConsole;
		bool writeToFile;
		std::ofstream fileOutputStream;

	public:
		Logger(std::string logFile, bool writeToConsole, bool writeToFile);
		~Logger();

		void debug(std::string message);

		void setWriteToConsole(bool enable);
		void setWriteToFile(bool enable);

		bool isWriteToConsole();
		bool isWriteToFile();

		Logger & operator << (const char *chararr);
		Logger & operator << (const std::string str);
		Logger & operator << (const int val);
};

#endif /* LOGGER_H_ */
