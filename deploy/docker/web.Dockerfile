FROM nginx:1.27-alpine

COPY deploy/docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY studyforge-frontend/apps/knowledge-web/dist/ /usr/share/nginx/html/knowledge/
COPY studyforge-frontend/apps/portal-web/dist/ /usr/share/nginx/html/portal/

EXPOSE 80
