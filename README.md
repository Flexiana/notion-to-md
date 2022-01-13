![GitHub License](https://img.shields.io/github/license/Flexiana/notion-to-md)

![Clojars Project](https://img.shields.io/clojars/v/com.flexiana/notion-to-md)


[Notion to Markdown](https://github.com/Flexiana/notion-to-md): a tool for fetching Notion page trees, written in Clojure. Can be used to sync Readme files.


## Concepts

Notion API requires a `page-id` and a `notion-secret` to be able to access a page’s content: 

- If the url is [`https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d`](https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d), then `page-id` is [`8ddeb7e276c34685b460c5380f592f9d`](https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d).

- `notion-secret` is obtained in Notion’s workspace configuration. Go to [My Integrations](https://www.notion.so/my-integrations) & add a New Integration. After you follow the steps there, you’ll end up with a secret string that looks like this: `secret_j2oz4j12ddjoalmdp91phesdahjlcsdwq0u11ay3Df8`. 


## **Usage**


### Lein

Add this to your dependencies:

```clojure
  [com.flexiana/notion-to-md "0.1.10"]

```

Alias it with the following:

```clojure
  :profiles {:local
               {:dependencies
                [[com.flexiana/notion-to-md "0.1.10"]]}}
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



## Outputs

- The main notion page (referenced by the NOTION_PAGE_ID environment variable) is going to generate the `README.md` file.

- The sub pages are going to be created inside the `docs/readme/` folder.


## Recommendations

1. Avoid using "link_to_page". Use “sub pages” instead. Or use “mention” if you want to make a link. Reason: A "link_to_page" is going to be inlined for technical reasons (the link's title is not easy to get). So, just use “sub pages” or “mentions” for the README files.


### Unsupported Notion types

`notion-to-md` does not support the types below:

1. "table_of_contents" doesn’t provide data.

1. "table" provides "unsupported" for each row.

1. "child_database" is complex and probably is not going to be used in a `README.md` so it's not supported by notion-to-md.

1. "link_preview"


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

Copyright 2022 Flexiana.

Written and distributed under [**Apache License 2.0**](https://github.com/Flexiana/notion-to-md/blob/development/LICENSE)**.**


## How to guides [internal]

[Update Readme on GitHub](https://www.notion.so/d1ecfe6b4bae41b1b9d22aceca9fb989) 



