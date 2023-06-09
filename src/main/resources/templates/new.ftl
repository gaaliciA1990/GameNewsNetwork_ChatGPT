<#import "_layout.ftl" as layout />
<@layout.header>
    <div>
        <h3>Create new article</h3>
        <form action="/articles" method="post">
            <p>
                Enter Title
            </p>
            <p>
                <input type="text" name="title" required>
            </p>
            <p>
                Enter Content
            </p>
            <p>
                <textarea name="body" required></textarea>
            </p>
            <p>
                Enter Publish Date <br> [YYYY-MM-DD HH:MM:SS]
            </p>
            <p>
                <input type="text" name="publish_date" required>
            </p>
            <p>
                <input type="submit">
            </p>
        </form>
    </div>
</@layout.header>