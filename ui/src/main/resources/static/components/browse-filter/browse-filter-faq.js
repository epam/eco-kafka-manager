
   const browserFilerFaq = '<div>\n' + 
       '<p>Filter how to:</p> \n' +
       '<ul> \n' +
           '<li><p>List of columns is populated after the first fetch. It contains all the fetched columns.</p></li> \n' +
           '<li><p>Filter are able to produce a "equals", "contains", "startWith", "like" and "not empty" operations</p></li> \n' +
           '<li><p>All filters clauses, that you choose, will be united by AND operator</p></li> \n' +
           '<li><p>In case the columns you have chosen have JSON or Map format, i.e. {a: {b: 123, c: "example"}} it is possible to use notation "column-name" equals "a.b: 123". This notation is applicable for \n' +
         '"equals", "contains", "startWith", "like" operations. It also works for a nested data and arrays i.e. {a: [{b: 123, c: "example"},{b:100, c: "example2"}]} => "column-name" equals "a.b: 123" also will choose this record.</p></li> \n' +
           '<li><p>Filter operates on only a set of records, which KM managed to read for given timeout. Offsets of these records indicated in a offset range table.  \n' +
               'If the set of record does not contain desired data, just press button ">" to search in a records with higher offsets. \n' +
               '</p> \n' +
           '</li> \n' +
       '</ul> \n' +    
   '</div>';    