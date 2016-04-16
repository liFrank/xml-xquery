/**
 * Define a grammar called Xquery
 */
grammar Xquery;

// XPath, entry point
xp  : ap
	| rp
	| f
	;    

// Xquery sub-language for the project, entry point
xq	: Var
	| String
	| ap
	| '(' xq ')'
	| xq ',' xq
	| xq '/' rp
	| xq '//' rp
	| '<' Name '>' '{' xq '}' '</' Name '>'
	| forClause (letClause | /*epsilon*/) (whereClause | /*epsilon*/) returnClause
	| letClause xq
	;
		
// absolute path 
ap 	: 'doc(' String ')/' rp 
	| 'doc(' String ')//' rp
	| 'document(' String ')/' rp 
	| 'document(' String ')//' rp 
	;
	
// relative path
rp	: Name
	| '*'
	| '..'
	| '.'
	| 'text()'
	| '@' Name
	| '(' rp ')'
	| rp '/' rp
	| rp '//' rp
	| rp '[' f ']'
	| rp ',' rp 
	;

//path filter
f	: rp
	| rp '=' rp
	| rp 'eq' rp
	| rp '==' rp
	| rp 'is' rp
	| '(' f ')'
	| f 'and' f
	| f 'or' f
	| 'not' f
	;	

forClause	: 'for' Var 'in' xq (',' Var 'in' xq)*;

letClause	: 'let' Var ':=' xq (',' Var ':=' xq)*;

whereClause	: 'where' cond;

returnClause: 'return' xq;

cond	: xq '=' xq
		| xq 'eq' xq
		| xq '==' xq
		| xq 'is' xq
		| 'empty(' xq ')'
		| 'some' Var 'in' xq (',' Var 'in' xq)* 'satisfies' cond
		| '(' cond ')'
		| cond 'and' cond
		| cond 'or' cond
		| 'not' cond
		;

// Literals must be capitalized

// attName, tagName
Name 		: ( 'a' .. 'z' | 'A' .. 'Z' | '_' )( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*;

// Var
Var			: '$' Name;

// fileName, StringConstant
String	: '"' ~('\n'|'\r')* '"';

// Ignore
//Comment 	:  '//' ~('\n')* '\n' -> skip;
  
WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

