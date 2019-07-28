FROM openjdk:8-alpine

RUN apk add --no-cache bash

COPY ./dist/wallblog /wallblog

WORKDIR /wallblog

CMD [ "/wallblog/bin/wallblog" ]