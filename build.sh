#!/bin/bash
 
# Some credit to https://github.com/maxtsepkov/bash_colors/blob/master/bash_colors.sh
#
# Constants and functions for terminal colors. Not using tput
# Author: Steve Wyckoff
 
COLOR_SCRIPT=color-logger.bash
COLOR_VERSION=0.9.0
 
COLOR_ESC="\x1B["
 
COLOR_RESET=0             # reset all attributes to their defaults
COLOR_RESET_UNDERLINE=24  # underline off
COLOR_RESET_REVERSE=27    # reverse off
COLOR_DEFAULT=39          # set underscore off, set default foreground color
COLOR_DEFAULTB=49         # set default background color
 
COLOR_BOLD=1              # set bold
COLOR_BRIGHT=2            # set half-bright (simulated with color on a color display)
COLOR_UNDERSCORE=4        # set underscore (simulated with color on a color display)
COLOR_REVERSE=7           # set reverse video
 
COLOR_BLACK=30            # set black foreground
COLOR_RED=31              # set red foreground
COLOR_GREEN=32            # set green foreground
COLOR_BROWN=33            # set brown foreground
COLOR_BLUE=34             # set blue foreground
COLOR_MAGENTA=35          # set magenta foreground
COLOR_CYAN=36             # set cyan foreground
COLOR_WHITE=37            # set white foreground
 
COLOR_BLACK_BG=40           # set black background
COLOR_RED_BG=41             # set red background
COLOR_GREEN_BG=42           # set green background
COLOR_BROWN_BG=43           # set brown background
COLOR_BLUE_BG=44            # set blue background
COLOR_MAGENTA_BG=45         # set magenta background
COLOR_CYAN_BG=46            # set cyan background
COLOR_WHITE_BG=47           # set white background
 
COLOR_DEBUG=$COLOR_BLUE
COLOR_INFO=$COLOR_MAGENTA
COLOR_HIGHLIGHT=$COLOR_CYAN
COLOR_WARN=$COLOR_BROWN
COLOR_ERROR=$COLOR_RED
 
COLOR_SUCCESS=$COLOR_GREEN
 
logger_wrap_escape() {
  local paint="$1"
  local message="$2"
 
  if ! [ $paint -ge 0 -a $paint -le 47 ] 2>/dev/null; then
    echo "escape: argument for \"$paint\" is out of range" >&2 && return 1
  fi
 
  if [ -z "$message" ]; then
    echo "No message passed in"
 
    exit 1
  fi
 
  message="${COLOR_ESC}${paint}m${message}${COLOR_ESC}${COLOR_RESET}m"
 
  echo -ne "\n$message\n"
}
 
logger_color(){
  local paint="$1"
  shift
 
  for message in "$@";
  do
    logger_wrap_escape "$paint" "$message"
  done
 
  echo
}
 
# PUBLIC API
debug(){
  logger_color "$COLOR_DEBUG" "$@"
}
 
info(){
  logger_color "$COLOR_INFO" "$@"
}
 
warn(){
  logger_color "$COLOR_WARN" "$@"
}
 
error(){
  logger_color "$COLOR_ERROR" "$@"
}
 
highlight(){
  logger_wrap_escape $COLOR_HIGHLIGHT "$1"
}
 
success(){
  logger_color "$COLOR_SUCCESS" "$@"
}
 
die () {
    error "$@"
    exit 1
}
 
[ "$#" -eq 1 ] || die "1 argument required, $# provided, please provide a version tag. $(highlight 'Usage: ./build.sh 1.0.0')"
 
debug "Updating Project Version"
mvn versions:set -DnewVersion=$1
 
debug "Building Gateway App v$1"
 
debug "Build docker image"
 
./mvnw clean package -Pprod -DskipTests dockerfile:build
 
debug "Tag to version v$1"
 
docker tag gateway:latest gateway:v$1
 
success "Congrats! Docker images are ready to deploy!"
success "$(highlight "./deploy.sh qa2ms $1") to deploy v$1 into qa2ms"
success "$(highlight "./deploy.sh prod1ms $1") to deploy v$1 into prod1ms"