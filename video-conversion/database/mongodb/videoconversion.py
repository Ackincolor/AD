
import logging

from pymongo import MongoClient
import ffmpy
import time
import os
import websocket
import json
import ssl
from azure.storage.file import FileService,ContentSettings
import os
import subprocess as sp
import sys
from ffmpeg_progress import start

logging.basicConfig(format='%(asctime)s - %(levelname)s: %(message)s', level=logging.DEBUG)
#  ffmpeg -i Game.of.Thrones.S07E07.1080p.mkv -vcodec mpeg4 -b 4000k -acodec mp2 -ab 320k converted.avi


class VideoConversion(object):
    def __init__(self, _config_):
        self.client = MongoClient(_config_.get_database_host())
        self.db = self.client[_config_.get_database_name()]
        self.video_conversion_collection = self.db[_config_.get_video_conversion_collection()]
        self.url = _config_.get_video_status_callback_url()

        self.file_service = FileService(account_name=_config_.get_azure_name(),account_key=_config_.get_azure_key());
    #test de l'ouverture pour azure storage




    def find_one(self):
        conversion = self.video_conversion_collection.find_one()
        uri = conversion['originPath']
        id = conversion['_id']
        file = self.file_service.get_file_to_path("archidistriconverter", None, uri, uri);
        logging.info('id = %s, URI = %s',  id, uri  )
        ff = ffmpy.FFmpeg(
                inputs={uri : None},
                outputs={'converted.avi' : '-flags +global_header -y -vcodec mpeg4 -b 4000k -acodec mp2 -ab 320k' }
            )
        logging.info("FFMPEG = %s", ff.cmd)
        # ff.run()
        self.video_conversion_collection.update({'_id' : id}, { '$set' : {'targetPath' : 'converted.avi'}})
        self.video_conversion_collection.update({'_id' : id}, { '$set' : {'tstamp' : time.time()  }})

        #for d in self.video_conversion_collection.find():
        #    logging.info(d)

    def ffmpeg_callback(infile: str, outfile: str, vstats_path: str):
        return sp.Popen(['ffmpeg',
                         '-nostats',
                         '-loglevel', '0',
                         '-y',
                         '-vstats_file', vstats_path,
                         '-i', infile,
                         outfile]).pid

    def on_message_handler(percent: float,
                           fr_cnt: int,
                           total_frames: int,
                           elapsed: float):
        sys.stdout.write('\r{:.2f}%'.format(percent))
        sys.stdout.flush

    def on_done_conversion(self, converted):
        self.send(converted);

    def convert(self, _id_, _uri_):
        file = self.file_service.get_file_to_path("archidistriconverter", None, _uri_, _uri_);
        converted = _uri_.replace(".mkv", "-converted.avi")
        logging.info('ID = %s, URI = %s —› %s',  _id_, _uri_ , converted )
        # ff = ffmpy.FFmpeg(
        #         inputs={_uri_: None},
        #         outputs={converted : '-flags +global_header -y -vcodec mpeg4 -b 4000k -acodec mp2 -ab 320k' }
        #     )
        # logging.info("FFMPEG = %s", ff.cmd)
        # ff.run()


        #self.send(converted);
        #suppression des fichiers locaux
        start(_uri_,
              converted,
              self.ffmpeg_callback,
              on_message= self.on_message_handler,
              on_done= lambda: self.on_done_conversion(converted),
              wait_time=1)
        #os.remove(_uri_)
        #os.remove(converted)

        #self.video_conversion_collection.update({'_id' : _id_}, { '$set' : {'targetPath' : converted}})
        #self.video_conversion_collection.update({'_id' : _id_}, { '$set' : {'tstamp' : time.time()  }})

        payload = dict()
        payload["id"] = _id_;
        payload["status"] = 0;

        json_payload = json.dumps(payload)
        logging.info("payload = %s", json_payload)

        ws = websocket.create_connection(self.url, sslopt={"cert_reqs": ssl.CERT_REQUIRED, "ca_certs" : "/home/lois/PycharmProjects/video-conversion/ca.cert.pem"})
        #ws = websocket.create_connection(self.url)
        ws.send(json_payload);
        ws.close()




    #send file to azure when conversion stoped
    def send(self,_uri_):
        self.file_service.create_file_from_path(
            'archidistriconverter',
            None,
            _uri_,
            _uri_,
            content_settings=ContentSettings(content_type='File')
        )


