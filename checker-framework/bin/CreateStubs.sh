#!/usr/bin/env bash

#######################
### INITIALIZATIONS ###
#######################

# Set a sane/secure path
PATH='/usr/local/bin:/bin:/usr/bin'

# It's almost certainly already marked for export, but make sure
\export PATH

# Clear all aliases. Important: leading \ inhibits alias expansion.
\unalias -a

# Clear the command path hash
hash -r

# Set the core dump limit to 0
ulimit -S -c 0 --

# Set a sane/secure IFS (note this is bash & ksh93 syntax only--not portable!)
IFS=$' \t\n'

# Set a sane/secure umask variable and use it
# Note this does not affect files already redirected on the command line
# 022 results in 0755 perms, 077 results in 0700 perms, etc.
UMASK=027
umask $UMASK

# Expand patterns, which match no files, to a null string, rather than themselves
shopt -s nullglob

####################
### DECLARATIONS ###
####################

### GLOBAL VARIABLES

### This variables can be queried, but must never be modified directly!

# Script version
declare -r version='1.0.3'

# Verbosity level:
declare be_verbose=false
declare -i verbosity_level=0

# Flag to become true, if a signal was caught to exit the script.
# To exit a potentially long running script gracefully,
# check this variable from time to time and if true, exit the script.
declare is_exit_signal=false

# Global temp directory
declare -r temp_dir_base="${TMP:=/tmp}"

# color code definitions:
declare -rA colors=( [gray]='\e[90m' [red]='\e[91m' [green]='\e[92m' [orange]='\e[33m' [blue]='\e[94m' [magenta]='\e[95m' [cyan]='\e[96m' [reset]='\e[0m' )

#########################
### UTILITY FUNCTIONS ###
#########################

# to be executed when the script exits:
function run_on_exit
{
	is_exit_signal=true
	# do custom cleanup here, if needed
	if [ -d "${temp_dir}" ] ; then
		rm -rf "${temp_dir}"
	fi
}

trap run_on_exit ABRT EXIT HUP INT QUIT

# Create a temporary directory.
# The directory path is stored in "${temp_dir},
# and it will be automagically cleaned up on script exit.
function create_temp_directory
{
	# check to create temp_dir only once:
	if [ -z "${temp_dir}" ] ; then
		test -d "${temp_dir_base}" || { echo "FATAL: \$TMP directory »${TMP}« not found." ; exit 123; }
		local -r prefix="${temp_dir_base}/${USER}.$(basename "$0")."
		local td="${prefix}${RANDOM}${RANDOM}${RANDOM}"
		while [ -d "${td}" ] ; do
			td="${prefix}.${RANDOM}${RANDOM}${RANDOM}"
		done
		if mkdir -m 0700 "${td}" ; then
			declare -gr temp_dir="${td}"
		else
			echo "FATAL: Failed to create temp dir »${td}«"
			exit 100
		fi
	fi
}

# Usage: color [-n] <color-code> arg ...
# Echo arguments in a given color.
# -n works like for echo
function color
{
	if [ "$1" = '-n' ] ; then
		local -r newline=false
		shift
	else
		local -r newline=true
	fi
	echo -ne "${colors[$1]}"
	shift
	echo -ne "$*${colors[reset]}"
	if $newline ; then
		echo
	fi
}

# Check availability of command »$1«.
# Optionally retrieve version of this command by either:
# parameters to the command »$1« given as »$2..$n« or:
# a shell function in »$2«.
function check_cmd
{
	if [ $# -ge 1 ] ; then
		if [ "$(type -t -- "$1")" = 'builtin' ] ; then
			color -n gray '· Using builtin' ; color orange " $1"
		else
			cmd="$(type -p "$1")"
			cmd_ver=''
			if [ -n "$cmd" ] ; then
				if [ $# -ge 2 ] ; then
					shift
					if [ "$(type -t -- "$1")" = 'function' ] ; then
						cmd_ver=" ($("$@"))"
					else
						cmd_ver=" ($($cmd "$@"))"
					fi
				fi
				color -n gray '· Using' ; echo -n " ${cmd}" ; color gray "${cmd_ver}"
			else
				echo "»${1}« not found! Please install first. Stop."
				exit 124
			fi
		fi
	else
		echo "Warning: function »check_cmd«: you gave me nothing to check, ignoring…"
	fi
}

# example get version function (see below):
function get_ffmpeg_version
{
	# select third word of output
	ffmpeg -version | head -n 1 | sed -E 's/^(\S+\s+){2}(\S+).*/\2/'
}

# check availability of commands needed by the script:
function check_necessary_commands
{
	check_cmd unzip
	check_cmd java --version
	check_cmd find
	check_cmd getopts
	check_cmd printf
	check_cmd true
	check_cmd false
	check_cmd trap
}

function show_version
{
	if [ $verbosity_level -ge 2 ] ; then
		check_necessary_commands
	fi
	if $be_verbose ; then
		echo "$(basename "$0") ${version}"
	else
		echo "$version"
	fi
}

#################################
### SCRIPT SPECIFIC FUNCTIONS ###
#################################

declare dry_run=false

function show_usage
{
	if $be_verbose ; then
		check_necessary_commands
		echo
	fi
	color -n orange 'USAGE: '
	color gray "$(basename "$0") [options] <Java simple class name> ..."
	echo '       Create Checker Framework stubs for the given *.java source files.'
	echo
	color orange 'OPTIONS:'
	echo
	color -n blue '-d : ' ; echo 'show only files which would be copied (dry run)'
	color -n blue '-h : ' ; echo 'show this help message'
	color -n blue '-r : ' ; echo 'run the main script action'
	color -n blue '-v : ' ; echo 'be verbose'
	color -n blue '-V : ' ; echo 'show script version and exit'
	echo '     -vh or -vvV checks necessary commands'
}

# example script action:
function main_script_action
{
	create_temp_directory
	cd "$(dirname "$0")/.." || exit 2
	if $be_verbose ; then
		echo "\$PWD = »$PWD«"
		echo "→ unzip sources to temp directory »${temp_dir}«"
	fi
	local -r subdir_lib='openjfx-27'
	if unzip -q ./src/"${subdir_lib}"/src.zip -d "${temp_dir}" ; then
		if [ $verbosity_level -ge 2 ] ; then eza -hlgF --color=auto --tree -L 2 "${temp_dir}" ; fi
		if $be_verbose ; then echo -n "→ Stubifying … " ; fi
		if java -cp ./lib/stubifier/checker.jar org.checkerframework.framework.stubifier.JavaStubifier "${temp_dir}" ; then
			echo "OK"
			if $is_exit_signal ; then exit 3 ; fi
			echo "→ Renaming …"
			find "${temp_dir}" -type f -name '*.java' -exec rename -l '.java' '.astub' '{}' \;
			if [ $verbosity_level -ge 2 ] ; then eza -hlgF --color=auto --tree "${temp_dir}" ; fi
			local d="$PWD"
			if pushd "${temp_dir}" ; then
				for f in "$@" ; do
					if $is_exit_signal ; then exit 3 ; fi
					if $dry_run ; then
						echo "»»» $f «««"
						find . -type f -name "$f.astub"
					else
						find . -type f -name "$f.astub" -exec cp -v --parents '{}' "${d}/stubs/${subdir_lib}" \;
					fi
				done
				popd || exit 2
			fi
		else
			if $is_exit_signal ; then
				echo
			else
				echo "ERROR!"
			fi
		fi
	fi
}

# script specific action:
declare action

while getopts 'dhrvV' arg
do
	case "$arg" in
		d) dry_run=true ;;
		h) action=show_usage ;;
		r) action=main_script_action ;;
		v) be_verbose=true ; ((verbosity_level++)) ;;
		V) action=show_version ;;
		?) show_usage ; exit 1 ;;
	esac
done
shift $((OPTIND - 1))

# run the script specific action:

if [ -n "$action" ] ; then
	$action "$@"
else
	show_usage
	exit 1
fi

