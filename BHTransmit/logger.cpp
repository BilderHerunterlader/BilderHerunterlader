/*
 * logger.cpp
 *
 *  Created on: 02.03.2010
 *      Author: Administrator
 */

#include "logger.h"
#include <fstream>
#include <iostream>

Logger::Logger(std::string logFile, bool writeToConsole, bool writeToFile) {
	this->writeToConsole = writeToConsole;
	this->writeToFile = writeToFile;
	this->logFile = logFile;

	if (writeToFile) {
		this->fileOutputStream.open(this->logFile.c_str(), std::ios::out);
		if (this->fileOutputStream.good() == false) {
			std::cout << "Could not open logfile" << std::endl;
		}
	}
}

Logger::~Logger() {
	this->fileOutputStream.close();
}

void Logger::debug(std::string message) {
	if (this->writeToConsole == true) {
		std::cout << message << std::endl;
	}
	if (this->writeToFile == true) {
		fileOutputStream << message << std::endl;
	}
}

void Logger::setWriteToConsole(bool enable) {
	this->writeToConsole = enable;
}

void Logger::setWriteToFile(bool enable) {
	this->writeToFile = enable;
	if (enable) {
		this->fileOutputStream.open(this->logFile.c_str(), std::ios::out);
		if (this->fileOutputStream.good() == false) {
			std::cout << "Could not open logfile" << std::endl;
		}
	}
}

bool Logger::isWriteToConsole() {
	return this->writeToConsole;
}

bool Logger::isWriteToFile() {
	return this->writeToFile;
}

Logger & Logger::operator << (const char *chararr) {
	if (this->writeToConsole == true) {
		std::cout << chararr;
	}
	if (this->writeToFile == true) {
		fileOutputStream << chararr;
	}
	return *this;
}

Logger & Logger::operator << (const std::string str) {
	if (this->writeToConsole == true) {
		std::cout << str;
	}
	if (this->writeToFile == true) {
		fileOutputStream << str;
	}
	return *this;
}

Logger & Logger::operator << (const int val) {
	if (this->writeToConsole == true) {
		std::cout << val;
	}
	if (this->writeToFile == true) {
		fileOutputStream << val;
	}
	return *this;
}
