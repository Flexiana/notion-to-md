`notion-to-md` is a tool that fetches Notion page tree. Turning them into usual Markdown files. Used to sync Readme.

It’s maintained at https://github.com/Flexiana/notion-to-md


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

This page contains an overview of our Readme workflow and how to get GitHub up to date.


## Workflow

We keep our technical documentation in Notion. This has been decided based on a number of reasons. All our documents now live under a single source of truth. Allowing a bird’s eye view. Notion offers the ease of change and discussion as well. It also keeps a handy history of changes, in case anything goes wrong. 

But we still like to have the usual technical documentation in the usual place in repo. Just to not make things confusing for newcomers and be prepared in case we go public. 

Which led us to periodically pulling Notion documents into markdown files. 


## When

One should import the Readme files from notion every month. For that we usually create a ticket in the backlog.


## How 

We keep the Readmes up to date using Untitled 

Below are the steps to take to save in git the most up to date Readme.


### Generate the Readme files for github

1. Go to Frankie’s GitHub's Actions [https://github.com/Flexiana/Frankie/actions](https://github.com/Flexiana/Frankie/actions)

1. Choose the “update-readme” workflow and click "Run workflow”

1. Click ‘...’, “View Workflow File” and then click the job’s button on the left sidebar to review the execution

1. Go to pull requests. There should be one new pull request to update the files. Review it and merge.


### Known issue and considerations

Unsupported: the Notion types below are not supported:

1. "table_of_contents" (:table_of_contents element) doesn’t provide data.

1. "table" provides "unsupported" for each row.

1. "child_database" is complex and probably is not going to be used at a [README.md](http://readme.md/) so it's not supported by the tool.

1. "link_preview" I wasn't able to generate a link-preview at Notion. If there is such a case, we could fix the tool.

1. If using images, please define the caption. Otherwise the tool is going to use random names for the images that will cause unwanted diffs on pull requests.

1. Avoid using "link_to_page". Use sub pages instead. Reason: A "link_to_page" is going to be inlined for technical reasons (the link's title is not easy to get). So, just use sub pages for the README files.










