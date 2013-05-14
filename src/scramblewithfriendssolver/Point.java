package scramblewithfriendssolver;

public class Point {

public int x;
public int y;

public Point(int x, int y) {
	this.x = x;
	this.y = y;
}

public Point(Point p) {
	this.x = p.x;
	this.y = p.y;
}

public boolean equals(Object o) {
	Point a = (Point) o;//this is what I needed to do, Object o
	if((this.x == a.x) && (this.y == a.y)) {
            return true;
        } else {
            return false;
        }
}
}

