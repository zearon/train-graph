#! /bin/bash
# Use __ as keyword to search __("Sample text") in java source code
xgettext --from-code=UTF-8 -k__ -o po/messages.pot `find . -name "*.java"`
msgmerge -U po/zh_CN.po po/messages.pot
msgmerge -U po/en_US.po po/messages.pot