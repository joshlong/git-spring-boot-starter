#!/usr/bin/env python
import http.server
import os

if __name__ == '__main__':
    generation_folder = os.path.join (os.environ['PODCAST_GENERATOR_OUTPUT_DIRECTORY'], 'git-clone-of-blog')
    print(generation_folder)
    handler_class = http.server.partial(http.server.SimpleHTTPRequestHandler, directory=generation_folder)
    http.server.test(HandlerClass=handler_class, port=9090, bind='localhost')
