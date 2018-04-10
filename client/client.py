#!/usr/bin/python

import base64
import hashlib
import json
import multiprocessing
import os
import socket
import sys
import threading
import time

from websocket import create_connection


# noinspection PyBroadException
class DemoThroughput:

    def __init__(self):
        self.chid = sys.argv[1]
        self.subscribers = int(sys.argv[2])
        pid = open('client.pid', 'w')
        pid.write(str(os.getpid()))
        pid.close()
        self.ignite_subscriber_threads()

    uid = '11386'
    api_key = '1654C8FC86A7CCA494DB1577D240198A'
    shared_key = '2006E0894779A35BB5586AA8E49CAEAB'
    lock = threading.Lock()

    befrest_gw = 'gw.bef.rest'
    befrest_api = 'api.bef.rest'
    endpoint_subscribe = '/xapi/%s/subscribe/%s/%s/%s'
    api_ver = '1'
    sdk_ver = '2'
    latencies = {}

    def generate_auth_token(self, payload):
        s = "%s,%s" % (self.api_key, payload)
        m = hashlib.md5()
        m.update(s)
        crypt = m.digest()
        crypt = base64.b64encode(crypt).replace('+', '-').replace('=', '').replace('/', '_')
        s = "%s,%s" % (self.shared_key, crypt)

        m = hashlib.md5()
        m.update(s)
        crypt = m.digest()
        auth = base64.b64encode(crypt).replace('+', '-').replace('=', '').replace('/', '_')
        return auth

    def subscription(self, auth, payload):
        headers = ['X-BF-AUTH: %s' % auth]
        url = 'ws://%s%s' % (self.befrest_gw, payload)
        ws = None

        try:
            ws = create_connection(url, header=headers)
            ws.sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_KEEPIDLE, 20)
            ws.sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_KEEPINTVL, 5)
            ws.sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_KEEPCNT, 2)

            while True:
                msg = ws.recv()
                self.ack_parse(ws, msg)
        except Exception as err:
            self.log('err-%s' % err)

            if ws is not None:
                ws.shutdown()

            self.subscription(auth, payload)

    def thread_handler(self, auth, payload, thread_count):
        threads = []

        for i in range(thread_count):
            threads.append(threading.Thread(
                name='sub.%d' % i, target=self.subscription, args={auth, payload}))

        [t.start() for t in threads]
        [t.join() for t in threads]

    def ignite_subscriber_threads(self):
        processes = []
        payload = self.endpoint_subscribe % (self.api_ver, self.uid, self.chid, self.sdk_ver)
        auth = self.generate_auth_token(payload)
#        cpu_count = multiprocessing.cpu_count()
	cpu_count = 100
        process_threads = int(self.subscribers / cpu_count)

        for _ in range(cpu_count):
            processes.append(multiprocessing.Process(target=self.thread_handler, args={auth, payload, process_threads}))

        [p.start() for p in processes]
        [p.join() for p in processes]

    def log(self, msg):
        print('%d\t%s' % (self.timestamp(), msg))

    @staticmethod
    def ack_parse(ws, msg):
        try:
            js = json.loads(msg)

            if 'mid' in js:
                payload = 'AN' + js['mid']
                ws.send(payload)
        except ValueError as _:
            print("Warning could not parse the received message!")
            print("received message is: " + msg)

    @staticmethod
    def timestamp():
        return int(round(time.time() * 1000))


if __name__ == "__main__":
    DemoThroughput()
