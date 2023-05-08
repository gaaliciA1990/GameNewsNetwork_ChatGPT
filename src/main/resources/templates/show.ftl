<#-- @ftlvariable name="article" type="com.example.models.Article" -->
<#import "_layout.ftl" as layout/>
<@layout.header>
    <div>
        <h3>
            ${article.title}
        </h3>
        <p>
            ${article.body}
        </p>
        <hr>
        <#if admin>
            <p>
                <a href="/articles/${article.id}/edit">Edit article</a>
            </p>
        </#if>
    </div>
</@layout.header>