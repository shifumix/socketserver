#docker run --restart=always -d -p 4567:4567 hhoareau/socketserver
FROM java:8
RUN apt-get update
RUN /var/lib/dpkg/info/ca-certificates-java.postinst configure

#ADD /out/server.crt /etc/ssl/certs
#ADD /out/server.key /etc/ssl/private
#ADD /out/hhoareau.jks .
#ADD /out/hhoareau.cer .
ADD /out/*.jar .

EXPOSE 4567
CMD ["java", "-cp", "./socketserver.jar","SockServer","https://shifumixweb.appspot.com"]