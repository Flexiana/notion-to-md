[Notion to Markdown](https://github.com/Flexiana/notion-to-md): a tool for fetching Notion page trees, written in Clojure. Can be used to sync Readme files.


## Concepts

Notion API requires a `page-id` and a `notion-secret` to be able to access a page’s content: 

- If the url is [`https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d`](https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d), then `page-id` is [`8ddeb7e276c34685b460c5380f592f9d`](https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d).

- `notion-secret` is obtained in Notion’s workspace configuration. Go to [My Integrations](https://www.notion.so/my-integrations) & add a New Integration. After you follow the steps there, you’ll end up with a secret string that looks like this: `secret_j2oz4j12ddjoalmdp91phesdahjlcsdwq0u11ay3Df8`. 


## **Usage**


### Lein

Addd thiss to your dependencies:

```clojure
  [com.flexiana/notion-to-md "0.1.2"]

```

Alias it with the following:

```clojure
  :profiles {:local
               {:dependencies
                [[com.flexiana/notion-to-md "0.1.2"]]}}
    :aliases {"notion-to-md"     
              ["with-profile" 
               "local" 
               "run" 
               "-m" 
               "notion-to-md.core"]}

```

Invoke it with `lein notion-to-md`. 

Pass arguments either by:

- `NOTION_PAGE_ID` and `NOTION_API_SECRET` environment variables. 

	```bash
    export NOTION_PAGE_ID="<page-id>"
    export NOTION_API_SECRET="<notion-secret>"
    lein notion-to-md

	```

	This is the way to go when integrating GitHub actions. See [https://docs.github.com/en/actions/learn-github-actions/environment-variables](https://docs.github.com/en/actions/learn-github-actions/environment-variables)


- Or as parameters:

	```bash
    lein notion-to-md <notion-secret> <page-id>

	```



### Allowing access to Notion API

To interact with the API, we have to give the integration user access to page(s) we want to fetch. To achieve this, follow the steps below.

1. Go to the page you want to fetch.

1. Click “Share” at the top-right corner.

1. Click “Invite”.

1. Select the Integration user you have added prior to this.


### Notes

The tool automatically fetches all sub pages as well.


### References

- Markdown reference: [https://www.markdownguide.org/basic-syntax/](https://www.markdownguide.org/basic-syntax/)

- API reference: [https://developers.notion.com/reference/block](https://developers.notion.com/reference/block)


## Authors

Brought to you with ❤️ by [Flexiana](https://flexiana.com/).


## License

Copyright 2022 Flexiana


## How to guides [internal]

[Update Readme on GitHub](https://www.notion.so/d1ecfe6b4bae41b1b9d22aceca9fb989) 

