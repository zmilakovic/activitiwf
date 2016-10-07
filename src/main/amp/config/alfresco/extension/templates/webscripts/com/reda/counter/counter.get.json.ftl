{
  "name" : <#if name?exists>"${name}"<#else>null</#if>,
  "value" : <#if value?exists>${value}<#else>0</#if>
}