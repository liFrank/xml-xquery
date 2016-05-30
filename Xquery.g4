/**
 * Define a grammar called Xquery
 */
grammar Xquery;

// Xquery sub-language for the project, entry point
xq	: Var																			#XQVar
	| String																		#XQString
	| ap																			#XQAp	
	| '(' xq ')'																	#XQParanth
	| xq ',' xq																		#XQWithXQ
	| xq '/' rp																		#XQChildren																			
	| xq '//' rp																	#XQBoth
	| '<' Name '>' '{' xq  '}' '</' Name '>'											#XQTag
	| forClause (letClause | /*epsilon*/) (whereClause | /*epsilon*/) returnClause	#XQFor
	| letClause xq																	#XQLet
	| 'join' '(' xq ',' xq ',' nameList ',' nameList ')'							#XQJoin
	;																	
		
// absolute path 
ap 	: 'doc' '(' String ')' '/' rp 			#APChildren
	| 'doc' '(' String ')' '//' rp			#APBoth
	| 'document' '(' String ')' '/' rp 		#APChildren
	| 'document' '(' String ')' '//' rp 	#APBoth
	;
	
// relative path
rp	: Name				#RPName
	| '*'				#RPAll
	| '..'				#RPParents
	| '.'				#RPCurrent
	| 'text()'			#RPText
	| '@' Name			#RPAttribute
	| '(' rp ')'		#RPParanth
	| rp '/' rp			#RPChildren
	| rp '//' rp		#RPBoth
	| rp ',' rp 		#RPWithRP
	| rp '[' f ']'		#RPWithFilter
	;

//path filter
f	: rp			#Filter
	| rp '=' rp		#FilterEqual
	| rp 'eq' rp	#FilterEqual
	| rp '==' rp	#FilterIs
	| rp 'is' rp	#FilterIs
	| '(' f ')'		#FilterParan
	| f 'and' f		#FilterAnd
	| f 'or' f		#FilterOr
	| 'not' f		#FilterNot
	;	

forClause	: 'for' Var 'in' xq (',' Var 'in' xq)*;

letClause	: 'let' Var ':=' xq (',' Var ':=' xq)*;

whereClause	: 'where' cond;

returnClause: 'return' xq;

cond	: xq '=' xq													#ConditionEqual
		| xq 'eq' xq												#ConditionEqual
		| xq '==' xq												#ConditionIs
		| xq 'is' xq												#ConditionIs
		| 'empty' '(' xq ')'										#ConditionEmpty
		| 'some' Var 'in' xq (',' Var 'in' xq)* 'satisfies' cond	#ConditionIn
		| '(' cond ')'												#ConditionParanth
		| cond 'and' cond											#ConditionAnd
		| cond 'or' cond											#ConditionOr
		| 'not' cond												#ConditionNot
		;
// attList
nameList	: '[' Name (',' Name)* ']';


// Literals must be capitalized

// attName, tagName
Name 		: ( 'a' .. 'z' | 'A' .. 'Z' | '_' )( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_'| '-' )*;

// Var
Var			: '$' Name;

// fileName, StringConstant
String	: '"' ~('\n'|'\r'|'"')* '"';

// Ignore
//Comment 	:  '//' ~('\n')* '\n' -> skip;
  
WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

