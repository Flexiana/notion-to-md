include .env

.PHONY: readme
readme:
	lein run 2> /dev/null
