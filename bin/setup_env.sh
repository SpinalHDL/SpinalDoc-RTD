#
# Use: source bin/setup_env.sh
#
unset _addPATH
for i in "$0" "$PWD/$BASH_SOURCE" "$PWD/$_"
do
	basedir=$(dirname "$i")
	if [ -f "${basedir}/convert-wrapper" ]
	then
		_addPATH="${basedir}"
		break
	elif [ -f "${basedir}/bin/convert-wrapper" ]
	then
		_addPATH="${basedir}/bin"
		break
	fi
done

if [ "X${_addPATH}" != "X" ]
then
	echo "Adding ${_addPATH} to \$PATH"
	export PATH="${_addPATH}:$PATH"
fi

if [ "X$sphinx_latest_version" = "X" ]
then
	# Pick the version considered to be the latest.
	echo "### git:"
	git for-each-ref --format "%(refname)" | sed 's/^refs\///g' | grep "tags/" | sed -e 's/^tags\///' | sort -g | tail -n1
	export sphinx_latest_version=$(git for-each-ref --format "%(refname)" | sed 's/^refs\///g' | grep "tags/" | sed -e 's/^tags\///' | sort -g | tail -n1)
	#echo sphinx_latest_version=$sphinx_latest_version
fi

echo "### ENV:"
declare -p | egrep "x sphinx_" | sed -e 's#declare -x \+##'
