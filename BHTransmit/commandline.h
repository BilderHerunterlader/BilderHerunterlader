/*
 * commandline.h
 *
 *  Created on: 02.03.2010
 *      Author: Administrator
 */

#ifndef COMMANDLINE_H_
#define COMMANDLINE_H_

#include "logger.h"
#include <string>

class CommandLine {
	private:
		std::string completeCommandLine;
		std::string paramFile;
		std::string paramUrl;
		std::string paramImg;
		bool paramDebug;
		bool paramsOK;

		std::string convertToString(char* chr);

	public:
		CommandLine(int argc, char *argv[], Logger &logger);

		std::string getCompleteCommandLine() const;
		std::string getParamFile() const;
		std::string getParamUrl() const;
		std::string getParamImg() const;
		bool isParamDebug() const;
		bool isParamsOK() const;
};

#endif /* COMMANDLINE_H_ */
