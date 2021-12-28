include .env

.PHONY: readme
readme:
	lein run 2> /dev/null
	git add README.md docs/
	git status
	@echo Now you can review with git diff --cached and commit the changes

install: 
	lein clean 
	lein pom
	lein install
