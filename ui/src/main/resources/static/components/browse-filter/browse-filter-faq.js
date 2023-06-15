
   const browserFilerFaq = '<div class="faq">\n' +
       '<p>Filter how to:</p> \n' +
       '<ul> \n' +
           '<li><p>List of columns is populated <b>after the first fetch</b>. It contains all the fetched columns.</p></li> \n' +
           '<li><p>Filter are able to produce a <b>"equals"</b>, <b>"contains"</b>, <b>"startWith"</b>, <b>"like"</b> and <b>"not empty"</b> operations</p></li> \n' +
           '<li><p>All filters clauses, that you choose, will be united by <b>AND</b> operator</p></li> \n' +
           '<li><p>In case the columns you have chosen have <b>JSON</b> or <b>Map</b> format, i.e. {a: {b: 123, c: "example"}} it is possible to use notation "column-name" equals "a.b: 123". This notation is applicable for \n' +
         '"equals", "contains", "startWith", "like" operations. <b>It also works for a nested data</b> and arrays i.e. {a: [{b: 123, c: "example"},{b:100, c: "example2"}]} => "column-name" equals "a.b: 123" also will choose this record.</p></li> \n' +
           '<li><p>Filter operates on only a set of records, which KM managed to read for <b>given timeout</b>. Offsets of these records indicated in a offset range table.  \n' +
               'If the set of record does not contain desired data, just press button ">" to search in a records with higher offsets. \n' +
               '</p> \n' +
           '</li> \n' +
           '<li><p>Operations <b>"exclude"</b> and <b>"only"</b> used only to a tombstones appearence: </p></li> \n' +
               '<p>"exclude" operation <b>exclude tombstones</b> from fetched records</p> \n' +
               '<p>"only" operation <b>shows only tombstone</b> messages</p> \n' +
       '</ul> \n' +    
   '</div>';    