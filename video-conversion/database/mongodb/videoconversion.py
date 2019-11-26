import logging

from pymongo import MongoClient
import ffmpy
import time
import os
import websocket
import json
import ssl
from azure.storage.file import FileService, ContentSettings
import pexpect
import re

logging.basicConfig(format='%(asctime)s - %(levelname)s: %(message)s', level=logging.DEBUG)


#  ffmpeg -i Game.of.Thrones.S07E07.1080p.mkv -vcodec mpeg4 -b 4000k -acodec mp2 -ab 320k converted.avi


class VideoConversion(object):
    def __init__(self, _config_):
        self.client = MongoClient(_config_.get_database_host())
        self.db = self.client[_config_.get_database_name()]
        self.video_conversion_collection = self.db[_config_.get_video_conversion_collection()]
        self.url = _config_.get_video_status_callback_url()

        self.file_service = FileService(account_name=_config_.get_azure_name(), account_key=_config_.get_azure_key());

    # test de l'ouverture pour azure storage

    def find_one(self):
        conversion = self.video_conversion_collection.find_one()
        uri = conversion['originPath']
        id = conversion['_id']
        file = self.file_service.get_file_to_path("archidistriconverter", None, uri, uri);
        logging.info('id = %s, URI = %s', id, uri)
        ff = ffmpy.FFmpeg(
            inputs={uri: None},
            outputs={'converted.avi': '-flags +global_header -y -vcodec mpeg4 -b 4000k -acodec mp2 -ab 320k'}
        )
        logging.info("FFMPEG = %s", ff.cmd)
        # ff.run()
        self.video_conversion_collection.update({'_id': id}, {'$set': {'targetPath': 'converted.avi'}})
        self.video_conversion_collection.update({'_id': id}, {'$set': {'tstamp': time.time()}})

        # for d in self.video_conversion_collection.find():
        #    logging.info(d)

    def timecode_value(self, tc):
        print("timecode_value :" + tc)
        hours, minutes, seconds = tc.split(':')
        return float(seconds) + (float(minutes) * 60) + (float(hours) * 60 * 60)

    def convert(self, _id_, _uri_):
        line = ""
        i = 0
        file = self.file_service.get_file_to_path("archidistriconverter", None, _uri_, _uri_);
        converted = _uri_.replace(".mkv", "-converted.avi")
        logging.info('ID = %s, URI = %s —› %s', _id_, _uri_, converted)
        ff = ffmpy.FFmpeg(
            inputs={_uri_: None},
            outputs={converted: '-flags +global_header -y -vcodec mpeg4 -b 4000k -acodec mp2 -ab 320k'}
        )
        logging.info("FFMPEG = %s", ff.cmd)
        # ff.run()
        cmd = ff.cmd
        thread = pexpect.spawn(cmd)
        print("started %s" % cmd)

        duration_total = 0

        while (not re.compile('^Press').match(line)):
            i = i + 1
            line = thread.readline().strip().decode('utf-8')
            if (re.compile('^Duration').match(line)):
                duration_total = self.timecode_value(line.split(',')[0].split(' ')[1])

        cpl = thread.compile_pattern_list([
            pexpect.EOF,
            "^(frame=.*)",
            '(.+)'
        ])
        while True:
            i = thread.expect_list(cpl, timeout=None)
            if i == 0:  # EOF
                self.video_conversion_collection.update({'_id': _id_}, {'$set': {'done': 100}})
                print("the sub process exited")
                break
            elif i == 1:
                try:
                    array = tuple(re.sub(r"=\s+", '=', thread.match.group(0).decode('utf-8').strip()).split(' '))
                    time = array[4]
                    tc, ts = tuple(time.split('='))
                    current_time = self.timecode_value(tc=ts)
                    percentage = (current_time / duration_total * 100)
                    print("Avancement : %.2f" % percentage)
                    self.video_conversion_collection.update({'_id': _id_},{'$set': {'done': percentage}})
                except:
                    print("exception")
                # self.video_conversion_collection.update({'_id': _id_}, {'$set': {'frame': frame_number.decode('utf-8').split("=")[1]}})
                thread.close
            elif i == 2:
                # unknown_line = thread.match.group(0)
                # print unknown_line
                pass

        # process = subprocess.Popen(shlex.split(cmd), stdout=subprocess.PIPE)
        # while True:
        #    output = process.stdout.readline()
        #    if output == '' and process.poll() is not None:
        #        break
        #    if output:
        #        print(output.strip())
        # rc = process.poll()

        self.send(converted);
        # suppression des fichiers locaux

        os.remove(_uri_)
        os.remove(converted)

        self.video_conversion_collection.update({'_id': _id_}, {'$set': {'targetPath': "Converted/"+converted}})
        # self.video_conversion_collection.update({'_id' : _id_}, { '$set' : {'tstamp' : time.time()  }})

        payload = dict()
        payload["id"] = _id_;
        payload["status"] = 0;

        json_payload = json.dumps(payload)
        logging.info("payload = %s", json_payload)

        ws = websocket.create_connection(self.url, sslopt={"cert_reqs": ssl.CERT_REQUIRED,
                                                           "ca_certs": "/home/lois/PycharmProjects/video-conversion/ca.cert.pem"})
        # ws = websocket.create_connection(self.url)
        ws.send(json_payload);
        ws.close()

    # send file to azure when conversion stoped
    def send(self, _uri_):
        self.file_service.create_file_from_path(
            'archidistriconverter',
            'Converted',
            _uri_,
            _uri_,
            content_settings=ContentSettings(content_type='File')
        )
