#!/bin/bash

DST="../jCoCoAnon"

if [ -e "$DST" ]; then
	echo "Target folder $DST already exists"
	exit 1;
fi

mkdir -p $DST/test/org/anon/cocoa/
mkdir -p $DST/src/org/anon/cocoa/

cp -r test/nl/coenvl/sam/* $DST/test/org/anon/cocoa/.
cp -r src/nl/coenvl/sam/* $DST/src/org/anon/cocoa/.

FILES=".classpath .project .gitignore LICENSE README.md createJar.jardesc"

for f in $FILES; do
	cp $f $DST/.
done

for f in `find $DST -name '*.java'` `find $DST -maxdepth 1 -type f` ; do
	sed -i 's/nl.coenvl.sam/org.anon.cocoa/g' $f
	sed -i 's/coenvanl@gmail.com/anon187c@gmail.com/g' $f
	sed -i 's/c.j.vanleeuwen-2@tudelft.nl/anon187c@gmail.com/g' $f
	sed -i 's/Coen van Leeuwen/Anomymous/g' $f
	sed -i 's/leeuwencjv/Anomymous/g' $f
	sed -i 's/Coen/Anomymous/g' $f
	sed -i 's/TNO/Anonymous/g' $f
	sed -i 's/SAM/CoCoA/g' $f
done
