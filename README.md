# Simple Data Warehouse

This is an HTTP API application with one simple function. Its function is to 
expose data while allowing to retrieve data as it is or using some simple aggregations
and conditions to provide answers to more complex queries. 

## Technologies used

- Java 8 SDK
- Groovy
- Grails 5
- Hibernate
- Gradle

## Application structure

Application is built using Grails 5 framework which has some conventions that needs to 
be followed and therefore these conventions also apply to this application so all the 
different components are where they should be. These components include controllers,
services, configuration, domain entities or tests.

## API Endpoints and interface
The sole purpose of this application is to provide data. These data are provided
using an HTTP API that has three different endpoints represented by three different
URIs.

## 1. Endpoint to list data based on query:
   ``
   POST /api/v1/statistics
   ``
- this endpoint exposes data saved in database. The structure of the data is:

```
TABLE 1: Data Attributes

attribute name   |  data type  | can be null |   explanation           
--------------------------------------------------------------
campaignName     |   string    |     no      |  name of the campaign
dataSourceName   |   string    |     no      |  name of the data source
clicks           |   long      |     no      |  number of clicks
impressions      |   long      |     no      |  number of impressions
ctr              |   decimal   |     no      |  clicks / impressions
date             |   date      |     no      |  format: MM/dd/YY
```

- consumes json object that contains all the query parameters, its structure corresponds 
to the properties of `simple.data.warehouse.dto.input.ApiQuery` class. We will call this
object ApiQuery from on.

- the ApiQuery object can contain these properties:
```
TABLE 2: object ApiQuery

attribute name |          data type           | can be null or empty 
--------------------------------------------------------------------
projections    | array of 'Projection' object |        yes
conditions     | array of 'Condition'  object |        yes
groupBy        | array of strings             |        yes
orderBy        | array of 'OrderBy' object    |        yes
batchSize      |           long               |        yes
offset         |           long               |        yes
```
### 1. Projections

`projections` property is an ordered array, that can be null, empty or can have 
any number of elements. These elements have to be an object of type `QueryProjection`. 
Objects of these type have these properties. 
The purpose of this object is to provide projections - combination of fields and possible
aggregation functions. Projections will define what kind of columns will be present in
result data set. Aliases can be used to give these columns customized name, otherwise 
property name is used. 
```
TABLE 3: object QueryProjection

attribute name | data type | can be null or empty 
--------------------------------------------------
type           |  string   |         no
attributeName  |  string   |         no
alias          |  string   |         yes
```
All projections are required to have a valid type and attribute name, but not all 
combinations are allowed. Forbidden combinations are in table below. Alias is not 
required but if it is used then needs to be a string containing only letters, numbers,
slash and underscore characters.

Possible `QueryProjection` types are these:

```
TABLE 4: Projection types

type       |        function                   |       forbiden attributes
---------------------------------------------------------------------------------------
ATTRIBUTE  |  provides attribute value as is   |
SUM        |   sum of all values in column     |    campaignName, dataSourceName
AVG        |   average of value in column      |  campaignName, dataSourceName, ctr
MIN        |   minimal value in column         |    campaignName, dataSourceName
MAX        |   maximal value in column         |    campaignName, dataSourceName
COUNT      |    count all columns              | 
```

### 2. Conditions

`conditions` property is an array containing objects of type `QueryCondition`. This 
array can be either null or empty but if there are no conditions provided, all the data will
be returned. The purpose of this object as the name might suggest is to provide lookup 
condition to get the data needed. 

```
TABLE 5: object QueryCondition

attribute name | data type | can be null or empty 
--------------------------------------------------
type           |  string   |         no
attributeName  |  string   |         no
value          |  string   |         no
```


Possible `QueryCondition` types are these:

```
TABLE 6: QueryCondition types

type   | meaning in context to condition value |       forbiden attributes
---------------------------------------------------------------------------------------
EQ     |       equals to value                 |
NE     |    not equals to value                |
LIKE   |  data value contains condition value  | clicks, impressions, ctr, date
LE     |   less or equals than value           |  campaignName, dataSourceName
GE     |   greater or equals than value        |  campaignName, dataSourceName
LT     |        less than value                |  campaignName, dataSourceName
GT     |        greater than value             |  campaignName, dataSourceName

```

Attribute name must be one of the available attributes mentioned in table nr.1.
Value in object `Conditions' must be in a correct format corresponding to the attribute's
data type (also see table nr.1).

### 3. GroupBy

`groupBy` is an array of ordered strings used for grouping by data sets retrieved 
by conditions in ApiQuery. These strings must be one of the attribute names or aliases
used in projections. If no projections are provided then this field is not used in query.

### 4. OrderBy

`orderBy` is an array that can be null or empty or can have any number of elements. All the 
elements must be of type `QueryOrderBy`. These objects are used to define ordering of
retrieved data set by specified attribute name and direction.

```
TABLE 7: object QueryOrderBy

attribute name | data type | can be null or empty 
--------------------------------------------------
attributeName  |  string   |         no
direction      |  string   |         no
```

Attribute name must be one of the attribute names of data attributes mentioned 
in table nr.1. Direction can be either `ASC` for ascending ordering or `DESC` for
descending ordering of result set data.

### Paging

If you need to query certain amount of rows or return rows starting from some position 
you can use parameters `batchSize` and `offset`. Both are not required to specify and 
when they are not specified then `batchSize` has default and maximum value of 1000 rows.
`offset` when not set has default value of 0. Both of these are of course numbers and of 
data type long.

### Response

Response is also a json object generated from groovy class
`simple.data.warehouse.dto.QueryResult`.

```
TABLE 7: object QueryResult

attribute name |      data type     | can be null or empty 
--------------------------------------------------
errorMessages  |  array of strings  |         yes
result         |  array             |         yes
```
Attribute `errorMessages` contains validation error messages if there is something 
wrong with the request ApiQuery object. 
Attribute `result` contains list of objects where every object represents a row of 
retrieved data set. Number, names and types of attributes of these objects depends on used 
projections in ApiQuery however they will be all one of those data attributes mentioned
in table nr.1

error response example:
```json
{
    "errorMessages": [
        "Cannot use member in order by because there is no attribute or used alias as this: avg_clicks"
    ],
    "result": null
}
```

## Endpoint to list all campaign names:

``
GET /api/v1/list/campaigns
``
- requires no parameters
- response is a json array with all campaign names available in data

## Endpoint to list all campaign names:

``
GET /api/v1/list/dataSources
``
- requires no parameters
- response is a json array with all data source names available in data

## Example API requests and responses for query endpoint (nr.1)

### 1.

request:
```json
{
    "batchSize" : 200,
    "offset" : 0,
    "projections": [
        {
            "type": "ATTRIBUTE",
            "attributeName": "date",
            "alias": "dt"
        },
        {
            "type": "ATTRIBUTE",
            "attributeName": "dataSourceName",
            "alias": "ds"
        },
        {
            "type": "MAX",
            "attributeName": "clicks",
            "alias": "max_clicks"
        }

    ],
    "conditions": [
        {
            "type": "EQ",
            "attributeName": "campaignName",
            "value": "Schutzbrief"
        },
        {
            "type": "GE",
            "attributeName": "date",
            "value": "12/02/19"
        },
        {
            "type": "LE",
            "attributeName": "date",
            "value": "12/06/19"
        }
    ],
    "groupBy": [
        "dataSourceName", "date"
    ],
    "orderBy": [
        {
            "attributeName": "ds",
            "direction": "ASC"
        },
        {
            "attributeName": "max_clicks",
            "direction": "DESC"
        }
    ]
}
```

and corresponding response:
```json
{
    "errorMessages": null,
    "result": [
        {
            "dt": "2019-12-06T00:00:00Z",
            "max_clicks": 0,
            "ds": "Facebook Ads"
        },
        {
            "dt": "2019-12-03T00:00:00Z",
            "max_clicks": 0,
            "ds": "Facebook Ads"
        },
        {
            "dt": "2019-12-04T00:00:00Z",
            "max_clicks": 0,
            "ds": "Facebook Ads"
        },
        {
            "dt": "2019-12-05T00:00:00Z",
            "max_clicks": 0,
            "ds": "Facebook Ads"
        },
        {
            "dt": "2019-12-02T00:00:00Z",
            "max_clicks": 0,
            "ds": "Facebook Ads"
        },
        {
            "dt": "2019-12-06T00:00:00Z",
            "max_clicks": 26,
            "ds": "Twitter Ads"
        },
        {
            "dt": "2019-12-02T00:00:00Z",
            "max_clicks": 25,
            "ds": "Twitter Ads"
        },
        {
            "dt": "2019-12-05T00:00:00Z",
            "max_clicks": 18,
            "ds": "Twitter Ads"
        },
        {
            "dt": "2019-12-03T00:00:00Z",
            "max_clicks": 10,
            "ds": "Twitter Ads"
        },
        {
            "dt": "2019-12-04T00:00:00Z",
            "max_clicks": 9,
            "ds": "Twitter Ads"
        }
    ]
}
```

### 2.

request:
```json
{
    "projections": [
        {
            "type": "ATTRIBUTE",
            "attributeName": "dataSourceName",
            "alias": "ds"
        },
        {
            "type": "AVG",
            "attributeName": "clicks",
            "alias": "avg_clicks"
        },
        {
            "type": "MAX",
            "attributeName": "impressions",
            "alias": "max_impr"
        },
        {
            "type": "COUNT",
            "attributeName": "impressions",
            "alias": "count"
        }

    ],
    "conditions": [
        {
            "type": "EQ",
            "attributeName": "campaignName",
            "value": "Schutzbrief"
        },
        {
            "type": "GE",
            "attributeName": "date",
            "value": "12/02/19"
        },
        {
            "type": "LE",
            "attributeName": "date",
            "value": "12/06/19"
        },
        {
            "type": "GE",
            "attributeName": "clicks",
            "value": "0"
        }
    ],
    "groupBy": [
        "dataSourceName"
    ],
    "orderBy": [
        {
            "attributeName": "dataSourceName",
            "direction": "ASC"
        },
        {
            "attributeName": "avg_clicks",
            "direction": "ASC"
        }
    ]
}
```

and corresponding response:
```json
{
    "errorMessages": null,
    "result": [
        {
            "count": 1,
            "avg_clicks": 0.0,
            "ds": "Facebook Ads",
            "max_impr": 267
        },
        {
            "count": 1,
            "avg_clicks": 0.0,
            "ds": "Facebook Ads",
            "max_impr": 137
        },
        {
            "count": 1,
            "avg_clicks": 0.0,
            "ds": "Facebook Ads",
            "max_impr": 275
        },
        {
            "count": 1,
            "avg_clicks": 0.0,
            "ds": "Facebook Ads",
            "max_impr": 147
        },
        {
            "count": 1,
            "avg_clicks": 9.0,
            "ds": "Twitter Ads",
            "max_impr": 112
        },
        {
            "count": 1,
            "avg_clicks": 10.0,
            "ds": "Twitter Ads",
            "max_impr": 156
        },
        {
            "count": 1,
            "avg_clicks": 18.0,
            "ds": "Twitter Ads",
            "max_impr": 257
        },
        {
            "count": 1,
            "avg_clicks": 25.0,
            "ds": "Twitter Ads",
            "max_impr": 354
        }
    ]
}
```