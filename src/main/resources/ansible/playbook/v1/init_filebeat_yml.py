#!/usr/bin/python
# coding=utf-8
import argparse
import base64
import json
import os
import sys
import urllib2

reload(sys)
sys.setdefaultencoding('utf-8')

parser = argparse.ArgumentParser(description='manual to this script')
parser.add_argument('--id', type=str, default=None)
parser.add_argument('--consul', type=str, default=None)
parser.add_argument('--path', type=str, default=None)
parser.add_argument('--version', type=str, default=None)
parser.add_argument('--type', type=str, default=None)
args = parser.parse_args()


def http_get():
    # url='http://'+args.consul+'/v1/kv/'+args.id+'.yml'
    url = 'http://' + args.consul + '/v1/kv/' + args.id
    print(url)
    req = urllib2.Request(url)
    response = urllib2.urlopen(req)
    return response


# 文件名不允许包含/ 所以替换为_
fileID = args.id.replace("/", "_")

data = http_get().read()
value = ((json.loads(data))[0])["Value"]
yml = base64.b64decode(value)

yml_filename = fileID + ".yml"
path = args.path + "/yml/"


def mkdir(path):
    folder = os.path.exists(path)
    if not folder:
        os.makedirs(path)
        print("---  new folder  ---")
        print("---  OK  ---")
    else:
        print("---  There is this folder!  ---")


mkdir(path)
yml_file_wtite = open(path + yml_filename, 'w')
ymlStr = bytes.decode(yml)

yml_file_wtite.write(ymlStr)
yml_file_wtite.close

service_filename = fileID + ".service"
lineBreak = "\n"
agent_parameters = ' -e -c /etc/filebeat/filebeat.yml -d "publish"'

temp = ['[Unit] ', lineBreak, 'Description=filebeat', lineBreak]
if args.type == "docker" or args.type == "k8s":
    temp.extend(['Requires=docker', lineBreak])
    temp.extend(['After=docker', lineBreak])

temp.extend(['[Service]', lineBreak,
             'User=root', lineBreak,
             'ExecStart=/usr/sbin/filebeat/filebeat-', args.version, agent_parameters, lineBreak,
             '[Install]', lineBreak,
             'WantedBy=multi-user.target', lineBreak])
service_content = ''.join(temp)

yml_file_wtite = open(path + service_filename, 'w')
yml_file_wtite.write(service_content)
yml_file_wtite.close
