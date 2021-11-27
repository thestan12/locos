FROM php:7.4-apache

ARG URL

RUN apt-get update
RUN apt install software-properties-common -y
RUN apt-get update
RUN add-apt-repository ppa:ondrej/php
RUN docker-php-ext-install pdo pdo_mysql


