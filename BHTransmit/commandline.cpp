/*
 * commandline.cpp
 *
 *  Created on: 02.03.2010
 *      Author: Administrator
 */
#include "commandline.h"
#include <string>
#include <sstream>

CommandLine::CommandLine(int argc, char *argv[], Logger &logger) {
	this->completeCommandLine = "";
	this->paramFile = "";
	this->paramUrl = "";
	this->paramImg = "";
	this->paramsOK = false;

	bool bFilename = false;
	bool bUrl = false;
	bool bImg = false;

	for (int i = 0; i < argc - 1; i++) {
		char* currentParam = argv[i];
		char* nextParam = argv[i + 1];
		std::string strCurrentParam = convertToString(currentParam);
		std::string strNextParam = convertToString(nextParam);

		if ( strCurrentParam.compare("-f") == 0 && strNextParam.length() > 0 ) {
			//if the parameter exists and the next parameter is not empty
			this->paramFile = nextParam;
			bFilename = true;
		} else if ( strCurrentParam.compare("-u") == 0 && strNextParam.length() > 0 ) {
			//if the parameter exists and the next parameter is not empty
			this->paramUrl = nextParam;
			bUrl = true;
		} else if ( strCurrentParam.compare("-i") == 0 && strNextParam.length() > 0 ) {
			//if the parameter exists and the next parameter is not empty
			this->paramImg = nextParam;
			bImg = true;
		}

		if (i > 0) {
			this->completeCommandLine += " ";
		}
		this->completeCommandLine += currentParam;
		if (i == (argc - 2)) {
			this->completeCommandLine += " ";
			this->completeCommandLine += nextParam;
		}
	}

	for (int i = 0; i < argc; i++) {
		char* currentParam = argv[i];
		std::string strCurrentParam = convertToString(currentParam);

		if ( strCurrentParam.find("-f ") == 0) {
			//if the parameter exists and the value is in the same parameter
			this->paramFile = strCurrentParam.substr(3);
			bFilename = true;
		} else if ( strCurrentParam.find("-u ") == 0) {
			//if the parameter exists and the value is in the same parameter
			this->paramUrl = strCurrentParam.substr(3);
			bUrl = true;
		} else if ( strCurrentParam.find("-i ") == 0) {
			//if the parameter exists and the value is in the same parameter
			this->paramImg = strCurrentParam.substr(3);
			bImg = true;
		}
	}

	if ((bFilename == true) xor (bUrl == true) xor (bImg == true)) {
		this->paramsOK = true;
	}

	logger.debug("CompleteCLI: " + this->completeCommandLine);
	logger.debug("ParamFile: " + this->paramFile);
	logger.debug("ParamUrl: " + this->paramUrl);
	logger.debug("ParamImg: " + this->paramImg);
	if (this->paramsOK) {
		logger.debug("Parameters OK: true");
	} else {
		logger.debug("Parameters OK: false");
	}
}

std::string CommandLine::getCompleteCommandLine() const {
	return this->completeCommandLine;
}

std::string CommandLine::getParamFile() const {
	return this->paramFile;
}

std::string CommandLine::getParamUrl() const {
	return this->paramUrl;
}

std::string CommandLine::getParamImg() const {
	return this->paramImg;
}

bool CommandLine::isParamsOK() const {
	return this->paramsOK;
}

std::string CommandLine::convertToString(char* chr) {
	std::stringstream strstream;
	strstream << chr;
	return strstream.str();
}
