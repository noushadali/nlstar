## Categories list ##
When someone enters the site, he will see categories of products as big rectangles. Each category has following attributes:
  * Displayed name
  * Fixed-size picture that will be displayed inside the rectangle
  * Rectangle background 1, which will be displayed by default
  * Rectangle background 2, which will substitute background 1 after user mouses over the category
  * Page background, which will substitute page's current background after _background\_change\_delay_ milliseconds after user mouses over the category.

Translation to the new rectangle background **and** page background will be smooth, specified by _smoothness_ parameter.

After user selects some category, all other categories that are closer to the left corner of the screen move the left and disappear when moving. All that remain, which are closer to the top - move to the top in similar way. All which are closer to the bottom - move to the bottom. The rest, which are to the right - move to the right.
After _selected\_category\_move\_delay_ milliseconds the selected category moves to the left, then - to the top. After it arrives, detailed description appears on the right. On the bottom the carousel of products appear smoothly, governed by _carousel\_appear\_smoothness_ millisecond parameter.

## Products carousel ##
|<img src='http://www.apinkprincess.com/images/P/mr_79851_worlds_fair_horse_carousel.jpg' />|Carousel is a bit tilted. It can be rotated by mouse. For each selected product the bigger picture appears in the bottom of the screen (smoothly), alongside with the description of the product as reach text.|
|:------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

## Edit item concept ##
When there is edit mode on particular entity, a form appears with all text data filled. For image fields, there are preview tiles with images (if any) and there are 'close' buttons near images. When such button is hit, title disappears and file input is displayed instead (inside the application a marker si set that particular image has being edited). If such marker is set, then on form submit corresponding input file will be submitted too. If its value is null then the image is considered deleted