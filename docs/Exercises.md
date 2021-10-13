Exercises
=========

Start by opening the application in the browser and opening the browser Dev Tools.

## Introductory exercises

### 1. Component tree

1. Open the **Components tab** of the dev tools (provided by the React dev tools)
2. Explore the tree of the components displayed
3. Select one of the `TodoItem` elements - can you see its props on the right side?

LESSON: The UI is indeed a tree of components.

### 2. Component props and query

React dev tools cannot show us the props of the component because it is a ClojureScript data structure. Let's have a look at them using Fulcro Inspect!

1. Open now the **Fulcro Inspect** tab and its **Element** sub-tab.
2. Click the _Pick Element_ in the top left corner and then click "Item 1" in the webapp
3. You should now see the details of the TodoItem, including
  * its "ident", meaning "identifier", namely `[:item/id 1]`
  * its props, including `:item/complete` and `:item/label`
  * its _query_ - notice how the props mostly mirror the query
4. Mark "Item 1" as complete by clicking to the left of its label and change its text by double-clicking on it and typing something then clicking outside of the list. How have the props changed?
  * Notice that for most purposes, having a prop with the value `false` and not having the prop at all are equivalent
5. Pick another list item element and compare its values with the original item
6. Select the whole `TodoList` and look at its props

LESSON: We have learned to use _Fulcro Inspect - Element_ to explore a component and we have learned that it has props, ident, and a query. We saw that the props mirror what is specified in the query.

### 3. Exploring the query

The query of a component declares its data needs and Fulcro uses it to build the props for the component. We have seen it using Fulcro Inspect - Element, now we will explore it using the code.

1. Open `src/fulcro_todomvc/ui.cljs`, scroll to the bottom and inside the `(comment ...)
   evaluate the `get-query` call, read the result
2. Now let's look at what the props look like - at the same place, eval `(-> ... (comp/props)))` Compare the props and the query
3. Repeat for `TodoList` (remember you can find its ident using the Element tab)

We see that `TodoList` queries for these props (some omitted): `[:list/id :list/items :list/title :list/filter]`.

But it also elaborates what it wants for each of the `:list/items` elements, namely `[:item/id :item/label :item/complete ...]`. How does it do that? By combining the two using a map, to produce a so called "join":

```clojure
#{:list/items [:item/id :item/label :item/complete :ui/editing :ui/edit-text]}
```

and includes this in its query instead of just `:list/items`. Now [look at the code](https://github.com/holyjak/fulcro-intro-wshop/blob/4992e994cb51bef46d6aaca5f7515da9c9536fb0/src/fulcro_todomvc/ui.cljs#L123) to see how it does that - it does not simply paste the child's query, it uses `(comp/get-query TodoItem)` to get it. This is important, because it include some important metadata. Let's have a look at it:

4. In `ui.cljs`, execute the `(binding ...)` form marked with _Exercise 3.4_.

EQL Primer: An EQL query includes 1) _properties_ such as `:list/label`, 2) joins of the form `{<property or ident> <query>}`, 3) or idents such as `[:item/id 1]` to ask for the data of the entity with that ident (and we can again use a join to precise what data)

LESSON: Components declare their data needs using `:query`, listing the properties they want. They _join_ in the query of their children using `get-query` to include the child's needs and thus to specify what properties of a nested data entity to include. The query also includes metadata that Fulcro needs for its processing.

### 4. Root query

Despite a common misconception, Fulcro does not supply props to every and each component individually. It only supplies props to the root component - and it uses only the root query, which composes the queries of its children and so on, as we have seen. So let's have a look at the query and how it is turned into a props tree.

1. Open `src/fulcro_todomvc/ui.cljs`, scroll to the bottom and inside the `(comment ...)
   evaluate the `get-query Root` call
2. It is little long and hard to read so open the [Shadow-cljs Inspect Latest](http://localhost:9630/inspect-latest) in a new tab of the same browser and then evaluate the `(tap> ...)` call. The value should appear in the Shadow Inspect; at the very bottom, click _Pretty-Print_
3. Now let's see how Fulcro fulfills that query from the client DB, using `fdn/db->tree` - execute the form marked _Exercise 4.3_ and observe the result in Shadow's Inspect Latest. First use _Pretty-Print_ on it then switch to _Browse_ - you can click on any line to "drill in" and you can use the `<` and `<<` to go (all the way) back

LESSON: The Root query is turned to a props tree using the client DB. Shadow Inspect is a fine tool for looking at complex data.

### 5. Exploring the client DB

Have the Root query somewhere where you can see it. We will use it to navigate the client DB to see how Fulcro builds the props tree.

1. Switch to _Fulcro Inspect_ - _DB Explorer_
2. At the very bottom, under _Top-Level Keys_ (which are all the keys in the DB that are not "entity tables", such as `:item/id`), there is `:root/todos` - which is also the beginning of the Root's query. Click on its value to "drill down". You will see the list 1 data map, displayed as table, with properties on the left and values on the right. Compare it to the query then drill down to one of the items. Notice that an item is not included in a list but referred to from the list using its ident.
3. Open _Fulcro Inspect_ - _DB_ and click on the little triangle ▶ to expand the `:list/id` and `:item/id` "tables". Now we can see the same data as before, but all at once.
4. Let's see now how the raw data of the client DB looks like. Go to `ui.cljs` and execute the _Exercise 5.4_ form, then switch to _Shadow - Inspect Latest_ to look at it and _Pretty-Print_ it.

LESSON: We saw how data is stored in the client DB mostly in a normalized form (`<entity name>/id -> <id value> -> <map of props, with idents as values to link to other entities>`) and how idents are used to link entities together. We have experienced how Fulcro fulfills a query by "walking" the client DB. We have seen that the client DB is nothing else than a map (of maps of maps, mostly).

----

**TO BE CONTINUED**

1. load!-ing data from the server, transactions
2. mutations

## Next steps

If you want to learn more Fulcro, study the [Minimalist Fulcro Tutorial](https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/) and do the accompanying exercises.