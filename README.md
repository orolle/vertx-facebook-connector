vertx-facebook-connector
========================
vertx-facebook-connector uses the facebook query language to query data from facebook. See the [fql documentation](https://developers.facebook.com/docs/technical-guides/fql/).


Default config:

    {
      address  : "vertx.facebook-connector" // eventbus address
    }


# FQL

This module only support fql at the moment. See the [fql documentation](https://developers.facebook.com/docs/technical-guides/fql/).


# Graph API

This module does not support the graph api from facebook to access the open graph.
Graph api is planed in future versions.

## FQL

Use FQL to get data from facebook.

### Inputs

    {
      action: "fql",
      access_token: "<FACEBOOK ACCESS TOKEN>",
      query: {
                "myName": "SELECT name, uid FROM user WHERE uid = me()",
                "myUrl": "SELECT url FROM url_like WHERE user_id IN (SELECT uid FROM #myName)"
              }
    }

### OUTPUTS

    {
      "myName":[
        {
          "name":"Prename Surname",
          "uid":1234567
        }
      ],
      "myUrl":[
        {
          "url":"http://www.youtube.com/"
        },
        {
          "url":"http://www.facebook.com/"
        }
      ]
    }

or

    {
      error: <message>
    }

