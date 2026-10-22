parser grammar ProfilesParser;

options {
    tokenVocab=ProfilesLexer;
}

script
	:	profile* EOF
	;

profile : PROFILE Id (ACTIVATE activeSlotName)? slotsDef;
slotsDef : (slotDef)* ;
slotDef : slotname (orderRule)? (itemDef)*?;
itemDef : itemName ('->' components )?;
itemName : itemIdLike ( customName )?;
customName: LPAREN STRING RPAREN;
components: LBRACK component (',' component)*? RBRACK;
component:  NamespacedId ( customName )?;
orderRule: 'order' STRING;
itemIdLike: NamespacedId | NamespacedTag | IdWildcard;

slotname : (HOT1|HOT2|HOT3|HOT4|HOT5|HOT6|HOT7|HOT8|HOT9|CHESTPLATE|LEGS|FEET|HEAD|OFFHAND);
activeSlotName : (HOT1|HOT2|HOT3|HOT4|HOT5|HOT6|HOT7|HOT8|HOT9);
