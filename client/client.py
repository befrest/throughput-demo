#!/usr/bin/python

import base64
import hashlib
import json
import multiprocessing
import os
import socket
import sys
import threading

from websocket import create_connection


# noinspection PyBroadException
class DemoThroughput:

    def __init__(self):
        self.chid = sys.argv[1]
        self.subscribers = int(sys.argv[2])
        self.process_count = multiprocessing.cpu_count()
        self.process_threads = self.subscribers / self.process_count

        if len(sys.argv) > 3 and int(sys.argv[3]) == 0:
            self.process_count = 1
            self.process_threads = self.subscribers

        pid = open('client.pid', 'w')
        pid.write(str(os.getpid()))
        pid.close()

        if self.process_threads < 1:
            self.process_threads = 1
            self.process_count = 1

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
            ws.sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_KEEPIDLE, 60)
            ws.sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_KEEPINTVL, 5)
            ws.sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_KEEPCNT, 2)
            ws.sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)

            while True:
                msg = ws.recv()
                self.ack_parse(ws, msg)
        except Exception as err:
            self.log('err-%s' % err)

            if ws is not None:
                ws.shutdown()

            self.subscription(auth, payload)

    def thread_handler(self, auth, payload):
        threads = []

        for i in range(self.process_threads):
            threads.append(threading.Thread(name='sub.%d' % i, target=self.subscription, args=(auth, payload)))

        [t.start() for t in threads]
        [t.join() for t in threads]

    def ignite_subscriber_threads(self):
        processes = []
        payload = self.endpoint_subscribe % (self.api_ver, self.uid, self.chid, self.sdk_ver)
        auth = self.generate_auth_token(payload)

        for i in range(self.process_count):
            processes.append(
                multiprocessing.Process(name="p.%d" % i, target=self.thread_handler, args=(auth, payload)))

        [p.start() for p in processes]
        [p.join() for p in processes]

    @staticmethod
    def log(msg):
        print(msg)

    def ack_parse(self, ws, msg):
        try:
            js = json.loads(msg)

            if 'mid' in js:
                payload = 'AN' + js['mid']
                ws.send(payload)
        except ValueError as _:
            print("Warning could not parse the received message!")
            print("received message is: " + msg)


if __name__ == "__main__":
    DemoThroughput()
