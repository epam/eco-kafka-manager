
   const browserFilerFaq = '<div>\n' + 
       '<p>Filter how to:</p> \n' +
       '<ul> \n' +
           '<li><p>List of columns has been populated after a first fetch. It contains by all fetched columns.</p></li> \n' +
           '<li><p>Filter are able to produce a "equals", "contains", "startWith", "like" and "not empty" operations</p></li> \n' +
           '<li><p>All filters clauses, that you choose, will be united by AND operator</p></li> \n' +
           '<li><p>In case of choosen column have JSON or Map format, i.e. {a: {b: 123, c: "example"}} it is possible to use notation "column-name" equals "a.b: 123". This notation possible in a \n' +
         '"equals", "contains", "startWith", "like" operations. It also works for a nested data and arrays i.e. {a: [{b: 123, c: "example"},{b:100, c: "example2"}]} => "column-name" equals "a.b: 123" also will choose this record.</p></li> \n' +
           '<li><p>Filter choose only that records witch KM has been read for given timeout. Offsets of these records indicated in a offset range table.  \n' +
               'If in this scope there is no desired records, just press button ">" to search in a records with higher offsets. \n' +
               '</p> \n' +
           '</li> \n' +
       '</ul> \n' +    
   '</div>';    