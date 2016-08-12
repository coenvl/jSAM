#!/bin/bash

DST="../jCoCoAnon"
GIT="anonymousgit:anonc187/jCoCoA.git"

if [ -e "$DST" ]; then
	echo "Target folder $DST already exists"
	#exit 1;
fi

# Clone git repository
git clone $GIT $DST
git -C $DST config user.name "Anon Y. Mous"
git -C $DST config user.email anon187c@gmail.com

echo "Cleaning up old repository"
#rm -rf $DST/*

echo "Copying working folder to $DST"
mkdir -p $DST/lib
#mkdir -p $DST/test/org/anon/cocoa/
#mkdir -p $DST/src/org/anon/cocoa/

#cp -r test/nl/coenvl/sam/* $DST/test/org/anon/cocoa/.
#cp -r src/nl/coenvl/sam/* $DST/src/org/anon/cocoa/.

FILES=".classpath .project .gitignore LICENSE README.md createJar.jardesc lib/*.jar"

for f in $FILES; do
	cp $f $DST/.
done

echo "Anonymizing $DST"
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

echo "Sending commits"
cd $DST
git add *
git commit -m `date +%Y-%m-%d`
git push origin master
