#! /bin/bash

MSGFMT=/usr/local/bin/msgfmt

for file in po/*.po ; do
	echo "Converting language file: " $file
	$MSGFMT --java2 -d $1 -r resources.Messages -l `echo $file | cut -f 1 -d '.' | cut -f 2 -d '/'` $file
done

