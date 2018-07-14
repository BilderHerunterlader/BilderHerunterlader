import os, sys, socket, getopt

def readportfromfile(portfile):
	portnumber = -1
	with open(portfile, 'r') as f:
		line = f.readline()
		try:
			portnumber = int(line)
		except ValueError as e:
			print('Could not parse port file: ' + str(e))
	return portnumber

def senddata(msg, host, port):
	success = False
	try:
		print('Creating socket...')
		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		print('Connection to {}:{}...'.format(host, port))
		s.connect((host, port))
		print('Sending data...')
		s.sendall(msg.encode('utf-8'))
		print('Closing connection...')
		s.close()
		print('Connection closed')
		success = True
	except socket.error as e:
		print('Error occured on send: ' + str(e))
	return success

textfile = None
containerurl = None
embeddedurl = None

usage = "Usage:\nBHTransmit.py parameter value\n\nParameters:\n-f\tFile containing Links (and Thumbnail-URLs)\n-u\tURL to a website which contains Links\n-i\tURL to website which contains embedded images\n-d\tTextfile for Debug-Messages\n\nExample:\nBHTransmit.py -u http://www.google.ch/\n"

try:
	opts, args = getopt.getopt(sys.argv[1:], "f:u:i:")
except getopt.GetoptError:
	print(usage)
	sys.exit(1)

for opt, arg in opts:
	if opt == '-f':
		textfile = arg
	elif opt == '-u':
		containerurl = arg
	elif opt == '-i':
		embeddedurl = arg

if not any((textfile, containerurl, embeddedurl)):
	print(usage)
	sys.exit(1)

portFile = os.path.join(os.path.expanduser('~'), '.BH', 'port.txt')
print('Reading port file: ' + portFile)
port = readportfromfile(portFile)
if port <= -1:
	print('No valid port found: ' + str(port))
	sys.exit(1)

msg = "SOF\n"
if textfile is not None:
	msg += textfile
elif containerurl is not None:
	msg += "URL:" + containerurl
elif embeddedurl is not None:
	msg += "IMG:" + embeddedurl
msg += "\nEOF"
if senddata(msg, '127.0.0.1', port):
	sys.exit(0)
else:
	sys.exit(1)
