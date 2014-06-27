#!/usr/local/bin/python3.4
# encoding: utf-8
import os.path
import signal
import subprocess
import sys

pidsDirectory = './logs/'

def getPIDFromFile(processName):
    filename = pidsDirectory + processName + ".PID"
    if (os.path.isfile(filename)):
        f = open(filename, 'r')
        pid = int(f.read())
        f.close()
        return pid
    return 0

def savePIDToFile(pid, processName):
    filename = pidsDirectory + processName + ".PID"
    f = open(filename, 'w')
    f.write(str(pid))
    f.close()

def killAndClean(pid, processName):
    try:
        os.kill(pid, signal.SIGTERM)
    except: OSError
    cleanAfterProcess(processName)

def cleanAfterProcess(processName):
    filename = pidsDirectory + processName + ".PID"
    os.remove(filename)

def start(processName, commandLine, killPreviousInstance):
    previousInstanceDead = True;
    pid = getPIDFromFile(processName)
    if (pid > 0):
        print("A process is already running. The new process will replace the old one.")
        previousInstanceDead = False
        
    if (previousInstanceDead):
        pid = subprocess.Popen(commandLine).pid
        savePIDToFile(pid, processName)   
    elif (killPreviousInstance):
        killAndClean(pid, processName)
        pid = subprocess.Popen(commandLine).pid
        savePIDToFile(pid, processName)
    
def stopProcess(processName):
    pid = getPIDFromFile(processName)
    if (pid == 0):
        print("The specified process seems to not be started")
    if (pid > 0):
        killAndClean(pid, processName)

commonPath = 'lib/*;'
args = {}

def startProcess(processName):
    if (processName in args.keys()):
        jarArguments = args[processName][0]
        mainClass = args[processName][1]
        programArguments = args[processName][2]
        start(processName, ['java', '-cp', commonPath + jarArguments, mainClass, programArguments], True)
    else:
        print("Unknown process. Valid process names: " + ', '.join("{!s}".format(k) for k in args.keys()))

args['activemq'] = ['activemq.jar', 'org.nuc.revedere.ActiveMQBroker', 'tcp://localhost:61616']

# start of script
operation = sys.argv[1]
processName = sys.argv[2]
if (operation == "start"):
    startProcess(processName)
elif (operation == "stop"):
    stopProcess(processName)
else:
    print("Unknown command. Recognized commands: start, stop")
