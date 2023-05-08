<#-- @ftlvariable name="articles" type="kotlin.collections.List<com.example.models.Article>" -->
<#import "_layout.ftl" as layout/>
<@layout.header>
    <#-- Display articles on page -->
    <#list articles as article>
        <div class="article">
            <h2>
                <a href="/articles/${article.id}">${article.title}</a>
            </h2>
            <p>
                ${article.body}
            </p>
        </div>
    </#list>

    <#-- Implement pagination links-->
    <div class="pagination">
        <#list 1..pageCount as i>
            <#if i == pageNumber>
                <span>${i}</span>
            <#else>
                <a href="/articles?page=${i}">${i}</a>
            </#if>
        </#list>
    </div>
    <hr>

    <#-- If admin is true, show the create article button-->
    <#if admin>
        <p>
            <a href="/articles/new">Create article</a>
        </p>
    </#if>
</@layout.header>
