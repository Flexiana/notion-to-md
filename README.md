
# notion-to-md Readme

> notion-to-md is a tool to import Notion pages to a Markdown format. It creates the Readme files that can be committed to you preferred git repository.


## Concepts

The Notion API requires a page-id and a notion-secret to provide the page’s content.

[Notion - The all-in-one workspace for your notes, tasks, wikis, and databases.](https://www.notion.so/)

Examples:

- If the url is [https://www.notion.so/Testnet-8ddeb7e276c34685b460c5380f592f9d](https://www.notion.so/8ddeb7e276c34685b460c5380f592f9d) for instance, the page-id is [8ddeb7e276c34685b460c5380f592f9d](https://www.notion.so/8ddeb7e276c34685b460c5380f592f9d)

- The notion-secret is obtained by the Notion’s workspace configuration. The secret its something like: secret_j2oz4j12ddjoalmdp91phesdahjlcsdwq0u11ay3Df8

[Notion - The all-in-one workspace for your notes, tasks, wikis, and databases.](https://www.notion.so/my-integrations)

```c
  int x = 3;

```


## **Usage**

There are some ways you can use this tool.

- Using it via clojars is the preferred way. Add the below to your project.clj file and then invoke it with `lein notion-to-md`

	```clojure
  :profiles {:local
               {:dependencies
                [[clj-http "3.12.3"]
                 [org.clojars.danielhvs/notion-to-md "0.1.0"]]}}
  :aliases {"notion-to-md"     
              ["with-profile" 
               "local" 
               "run" 
               "-m" 
               "notion-to-md.core"]}

	```


- Using environment variables: NOTION_PAGE_ID and NOTION_API_SECRET and then invoking it directly

	```bash
  export NOTION_PAGE_ID="<page-id>"
  export NOTION_API_SECRET="<notion-secret>"
  lein run

	```


- Passing as the id and secret as parameters:

	```bash
  lein run <notion-secret> <page-id>

	```


