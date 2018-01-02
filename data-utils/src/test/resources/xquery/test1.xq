xquery version "1.0" encoding "UTF-8";


declare namespace dtt = "http://data.test"
declare namespace dat = "http://data"
declare namespace book = "http://book"
 
declare function dtt:Test1($input1 as element(), $input2 as xs:string)
    as element(ns0:startRequest) {
		<book:Collection>
			<book:Name>Fiction</book:Name>
			<book:Book>
				<book:Title>The Hobbit</book:Title>
				<book:Author>JRR Tolkien</book:Author>
				<book:PublishDate>1937-09-21</book:PublischDate>
			</book:Book>
			<book:Book>
				<book:Title>The Two Towers</book:Title>
				<book:Author>JRR Tolkien</book:Author>
				<book:PublishDate>1954-11-11</book:PublischDate>
			</book:Book>
		</book:Collection>
};

declare variable $input1 as element() external;
declare variable $input2 as element() external;

dtt:Test1($input1,$input2)

