
import pika
from threading import Thread
import logging
from google.cloud import pubsub_v1
import time
import json
import queue

logging.basicConfig(format='%(asctime)s - %(levelname)s: %(message)s', level=logging.DEBUG)
logging.getLogger("pika").setLevel(logging.INFO)

# rabbitmqadmin -H localhost -u ezip -p pize -V ezip purge queue name=video-conversion-queue
# rabbitmqadmin -H localhost -u ezip -p pize -V ezip get queue=video-conversion-queue

class VideoConversionMessaging(Thread):
    def __init__(self,_config_,converting_service):
        Thread.__init__(self)
        self.converting_service = converting_service
        subscriber = pubsub_v1.SubscriberClient()
        subsciption_path = subscriber.subscription_path(_config_.get_project_id(),_config_.get_subscription_name())
        subscrition_project = subscriber.project_path(_config_.get_project_id());
        for subscription in subscriber.list_subscriptions(subscrition_project):
            print(subscription)
        def callback(message):
            print('Received message: {}'.format(message.data.decode('utf-8')))
            message.ack()
            self._on_message_(message)

        subscriber.subscribe(subsciption_path, callback=callback)

        print('listening fo message on {}'.format(subsciption_path))
        while True:
            time.sleep(60)
#
#
#
#
#     def on_message(self, channel, method_frame, header_frame, body):
#         logging.info(body)
#         # logging.info('id = %s, URI = %s', body["id"], body['originPath'])
#         # logging.info('URI = %s', body['originPath'])
#         logging.info('URI = %s', body.decode())
#         convert_request = json.loads(body.decode())
#         logging.info(convert_request)
#         self.converting_service.convert(convert_request["id"], convert_request['originPath'])
    def _on_message_(self,  body):
         logging.info(body)
         # logging.info('id = %s, URI = %s', body["id"], body['originPath'])
         # logging.info('URI = %s', body['originPath'])
         logging.info('URI = %s', body.data.decode())
         convert_request = json.loads(body.data.decode())
         logging.info(convert_request)
         self.converting_service.convert(convert_request["id"], convert_request['originPath'])
#
#
#

