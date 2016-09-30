#!/bin/bash
DST="anonymous"
GIT="git@github.com:anonc187/jCoCoA.git"

if [ -e "$DST" ]; then
	echo "Target folder $DST already exists"
	exit 1;
fi

# Clone git repository
git clone $GIT $DST
git -C $DST config user.name "Anon Y. Mous"
git -C $DST config user.email anon187c@gmail.com

# Remove all content so we also will delete old files
echo "Cleaning up old repository"
rm -rf $DST/*

# Prepare folders
echo "Copying working folder to $DST"
mkdir -p $DST/src/main/java/org/anon/cocoa/
mkdir -p $DST/src/test/java/org/anon/cocoa/

# Copy folders
cp -r src/main/java/nl/coenvl/sam/* $DST/src/main/java/org/anon/cocoa/.
cp -r src/test/java/nl/coenvl/sam/* $DST/src/test/java/org/anon/cocoa/.

# Copy files
FILES=".classpath .project .gitignore build.gradle LICENSE"
for f in $FILES; do
	cp $f $DST/.
done

# Copy README
head -n -4 README.md > $DST/README.md

# Anonymize
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

# Commit and push
echo "Sending commits"
git -C $DST add -A
git -C $DST commit -m `date +%Y-%m-%d`
git -C $DST push origin master
