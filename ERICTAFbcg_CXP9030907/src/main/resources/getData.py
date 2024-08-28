import sys
import os
fileName = sys.argv[1]

def StartTag(line):
        line=line.strip()
        if line=="":
                #debug("StartTag received Empty Line")
                return False
        #debug("Line is "+line)
        if line[0]=="<" and line[-1]==">" and ("<" not in line[1:-2]) and line[1]!="/" and line[-2]!="/" and "id=" in line:
                #debug("StartTag Returning True")
                return True
        else:
                #debug("Start Tag Reutring False")
                return False
                

filePath = "/var/opt/ericsson/nms_umts_wran_bcg/files/import/" + fileName
#filePath = fileName
fileObject = open(filePath, "r+")

fileLines = fileObject.readlines()
FdnFromFile = ""
for line in fileLines : 
        if line.strip()=="": continue
        line = line.strip()
        if StartTag(line):
                arrOfStr = line.strip().split(" ")
                MoStr = arrOfStr[0]
                MoName = MoStr.split(":")[1].strip()
                for things in arrOfStr:
                        if "id=" in things:
                                IdStr=things
                                Id = IdStr.split("=")[1]
                                IdNew = ""
                                for ch in Id:
                                        if ch=="\"" : continue
                                        if ch==">" : continue
                                        IdNew = IdNew + ch
                                Id = IdNew
                                break
                if MoName ==("ExternalUtranCell") :
                        FdnFromFile = FdnFromFile + MoName+"="+Id;
                        break
                else:
                        FdnFromFile = FdnFromFile + MoName+"="+Id+",";
        
#print FdnFromFile
                
os.system("/opt/ericsson/nms_cif_sm/bin/smtool -action cms_snad_reg getProxiesForMaster MoFDN  "+FdnFromFile+">ProxiesMo")
fileObject = open("ProxiesMo","r+")
fileLines = fileObject.readlines()
fileObject.close()
fdnList = list(fileLines)
fdnFinal = []
print "Proxy MOs for master MO is "
for fdn in fdnList :
        print fdn
        if not "SubNetwork".lower() in fdn.lower() : continue
        fdn2=""
        for i in fdn:
                if i=="[":continue
                if i=="]":continue
                fdn2=fdn2+i
        print fdn2
        fdnFinal.append(fdn2)

for fdn in fdnFinal :
        os.system("/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice la "+fdn.strip()+" reservedBy")
        
        
        

