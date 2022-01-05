`notion-to-md` is a tool that fetches Notion page tree. Turning them into usual Markdown files. Used to sync Readme.

It’s maintained at [https://github.com/Flexiana/notion-to-md](https://github.com/Flexiana/notion-to-md)


## Concepts

The Notion API requires a `page-id` and a `notion-secret` to provide a page’s content: 

- If the url is [`https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d`](https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d), then the page-id is [`8ddeb7e276c34685b460c5380f592f9d`](https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d).

- `notion-secret` is obtained in Notion’s workspace configuration. The secret string looks something like `secret_j2oz4j12ddjoalmdp91phesdahjlcsdwq0u11ay3Df8`.

	[Notion Integration](https://www.notion.so/my-integrations)



## **Usage**


### Lein

Add this to your dependencies:

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

	You can even integrate it with GitHub's actions, exposing the environment variables. See [https://docs.github.com/en/actions/learn-github-actions/environment-variables](https://docs.github.com/en/actions/learn-github-actions/environment-variables)


- Passing id and secret as parameters:

	```bash
    lein notion-to-md <notion-secret> <page-id>

	```





### Allowing access to Notion’s API

Notion must allow users to interact with the API so we have to allow the “integration user” to access the pages:

Choose the main Notion’s README page and “share it”:

1. Click “Share” at top right corner

1. Click the “Invite” button.

1. Select the “integration user” user.

Sub pages are going to be automatically shared.


### References

- Markdown reference: [https://www.markdownguide.org/basic-syntax/](https://www.markdownguide.org/basic-syntax/)

- API reference: [https://developers.notion.com/reference/block](https://developers.notion.com/reference/block)


## How to guides

<<<<<<< Updated upstream
Update Readme on GitHub 
=======
[Update Readme on GitHub](https://www.notion.so/d1ecfe6b4bae41b1b9d22aceca9fb989) 
>>>>>>> Stashed changes

