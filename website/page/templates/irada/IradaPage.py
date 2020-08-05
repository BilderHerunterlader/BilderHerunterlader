import os, time, shutil, codecs, re

patternImg = r'(<img.+)src="(.*?)"'
replaceImg = r'\1src="irada/\2"'
patternIco = r'\.\./host\.ico/'
replaceIco = r'host_ico/'
patternHref = r'href="irada(.*?)"'
replaceHref = r'href="irada/irada\1"'
patternHrefAnleitung = r'href="Anleitung\.html(.*?)"'
replaceHrefAnleitung = r'href="?loc=irada/anleitung\1"'
patternHrefIrada = r'href="Irada\.html(.*?)"'
replaceHrefIrada = r'href="?loc=irada\1"'
patternHrefHostini = r'href="Beschreibung_hostini\.html(.*?)"'
replaceHrefHostini = r'href="?loc=irada/hostini\1"'

def convertContent():
	sourceFile = 'Irada.html'
	targetFile = 'index.tpl'

	convertPage(sourceFile, targetFile)

def convertHost():
	sourceFile = 'Beschreibung_hostini.html'
	targetFile = 'hostini.tpl'

	convertPage(sourceFile, targetFile)

def convertAnleitung():
	sourceFile = 'Anleitung.html'
	targetFile = 'anleitung.tpl'

	convertPage(sourceFile, targetFile)

def convertPage(sourceFile, targetFile):
	fIn = codecs.open(sourceFile, 'r')
	fOut = codecs.open(targetFile, 'w', "utf-8")
	
	fOut.write("{literal}\n")
	fOut.flush()

	started = False
	stopped = False
	i = 0
	iStarted = -1
	for line in fIn:
		data = line
		if data.endswith('<div class="irada">\n') and not started:
			started = True
			iStarted = i
		if data.endswith('</body></html>') and not stopped:
			stopped = True
		if started and not stopped and checkStartIndex(i, iStarted):
			data = correctLine(data)
			fOut.write(data)
			fOut.flush()
		i += 1

	fOut.write("{/literal}\n")
	fOut.flush()
	
	fIn.close()
	fOut.close()

def correctLine(line):
	corrected = line
	if 'src="<?=$path?>' not in corrected:
		corrected = re.sub(patternImg, replaceImg, corrected)
		corrected = re.sub(patternIco, replaceIco, corrected)
	if 'href="<?=$path?>' not in corrected:
		corrected = re.sub(patternHref, replaceHref, corrected)

	corrected = re.sub(patternHrefIrada, replaceHrefIrada, corrected)
	corrected = re.sub(patternHrefAnleitung, replaceHrefAnleitung, corrected)
	corrected = re.sub(patternHrefHostini, replaceHrefHostini, corrected)

	return corrected

def checkStartIndex(index, started):
	if started < 0:
		return True
	if index == (started + 1):
		return False
	if index == (started + 2):
		return False
	if index == (started + 3):
		return False
	return True

convertContent()
convertAnleitung()
convertHost()
