#!/usr/bin/python
# coding=utf-8
import sys
import urllib2
import argparse
import json
import base64
import os

reload(sys)
sys.setdefaultencoding('utf-8')

parser = argparse.ArgumentParser(description='manual to this script')
parser.add_argument('--id', type=str, default = None)
parser.add_argument('--consul', type=str, default = None)
parser.add_argument('--path', type=str, default = None)
parser.add_argument('--version', type=str, default = None)
parser.add_argument('--project', type=str, default = None)
args = parser.parse_args()

def http_get():
    # url='http://'+args.consul+'/v1/kv/'+args.id+'.yml'
    url='http://'+args.consul+'/v1/kv/'+args.id
    print(url)
    req = urllib2.Request(url)
    response = urllib2.urlopen(req)
    return response

data=http_get().read()
value=((json.loads(data))[0])["Value"]
yml = base64.b64decode(value)


yml_filename=args.id+".yml"
path=args.path+"/yml/"

def mkdir(path):
    folder = os.path.exists(path)
    if not folder:
        os.makedirs(path)
        print("---  new folder  ---")
        print("---  OK  ---")
    else:
        print("---  There is this folder!  ---")

mkdir(path)
yml_file_wtite =open(path+yml_filename,'w')
ymlStr=bytes.decode(yml)

yml_file_wtite.write(ymlStr)
yml_file_wtite.close

service_filename=args.id+".service"
lineBreak = "\n"
agent_parameters = ' -e -c /etc/filebeat/'+args.id+'.yml -d "publish"'

temp = ['[Unit] ',lineBreak,
        'Description=filebeat',lineBreak,
        '[Service]',lineBreak,
        'User=root',lineBreak,
        'ExecStart=/usr/sbin/filebeat/',args.project,'/filebeat-',args.version,agent_parameters,lineBreak,
        '[Install]',lineBreak,
        'WantedBy=multi-user.target',lineBreak]
service_content=''.join(temp)

yml_file_wtite =open(path+service_filename,'w')
yml_file_wtite.write(service_content)
yml_file_wtite.close