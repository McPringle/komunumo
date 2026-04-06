# Allow changes to pom.xml made by setup-testlens action
R=`git status --porcelain | grep --invert " pom.xml" | wc -l`
if [ "$R" -ne "0" ]; then
  echo "The git repository is not clean after compiling and testing. Did you forget to commit or ignore files?";
  git status --porcelain
  exit 1;
fi
