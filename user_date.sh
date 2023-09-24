#!/usr/bin/bash
# use this for user data
yum update -y
yum install -y httpd
systemctl start httpd
systemctl enable httpd
date +'FORMAT'
date +'%m/%d/%Y'
date +'%r'
backup_dir=$(date +"%D %T")
echo "<h1>Hello from Ravi's Amazon Linux EC2 instance -> $(hostname -f) and time is ${backup_dir}</h1>" >> /var/www/html/index.html
