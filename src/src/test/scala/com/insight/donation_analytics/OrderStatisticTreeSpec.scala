/*
 * Copyright 2018 Zhang Di
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.insight.donation_analytics

import scala.language.implicitConversions
import scala.util.Random
import OrderStatisticTree.Color
import OrderStatisticTreeSpec._

class OrderStatisticTreeSpec extends UnitSpec {

  def height(rbt: OrderStatisticTree): Int = {
    rbt match {
      case Leaf => 0
      case Node(_, _, _, l, r) => 1 + math.max(height(l), height(r))
    }
  }

  def count(rbt: OrderStatisticTree): Int = {
    rbt match {
      case Leaf => 0
      case Node(_, _, _, l, r) => 1 + count(l) + count(r)
    }
  }

  def check(rbt: OrderStatisticTree)(f: OrderStatisticTree => Boolean): Unit = {
    rbt match {
      case Leaf => f(Leaf) should be (true)
      case Node(_,_,_,l,r) =>
        f(rbt) should be (true)
        check(l)(f)
        check(r)(f)
    }
  }

  def inOrderSeq(rbt: OrderStatisticTree): Seq[(Long, Color, Int)] = {
    rbt match {
      case Leaf => Seq[(Long, Color, Int)]()
      case Node(x, c, n, l, r) =>
        inOrderSeq(l) ++ Seq[(Long, Color, Int)]((x, c, n)) ++ inOrderSeq(r)
    }
  }

  def printTree(rbt: OrderStatisticTree, indent: Int): String = {
    rbt match {
      case Leaf => "Leaf"
      case Node(x, c, cnt, l, r) =>
        s"\n${"\t"*indent}Node($x, $c, $cnt, ${printTree(l, indent + 1)}, ${printTree(r, indent + 1)})"
    }
  }

  "A OrderStatisticTree" should "be balanced if the input is sorted" in {
    (1 to 20).foreach{n =>
      val rbt = OrderStatisticTree(1 until (1 << n): _*)
      val h = height(rbt)
      logger.debug(s"sorted design height: $n actual height: $h")
      h should be <= 2 * n
    }
  }

  it should "be balanced if the input is random" in {
    (1 to 20).foreach{n =>
      val sorted = 1 until(1 << n)
      val shuffled = Random.shuffle(sorted.toList)
      val rbt = OrderStatisticTree(shuffled: _*)
      val h = height(rbt)
      logger.debug(s"random design height: $n actual height: $h")
      h should be <= 2 * n
    }
  }

  it should "maintain the order correctly" in {
    val h = 10
    val input = 0 until (1 << h)
    val rbt1 = OrderStatisticTree(input: _*)
    val rbt2 = OrderStatisticTree(Random.shuffle(input.toList):_*)

    val seq1 = inOrderSeq(rbt1)
    val seq2 = inOrderSeq(rbt2)

    seq1.length should be (1 << h)
    seq2.length should be (1 << h)

    seq1.zip(0 until (1 << h)).foreach(p => p._1._1 should be (p._2))
    seq2.zip(0 until (1 << h)).foreach(p => p._1._1 should be (p._2))

  }

  it should "maintain the augmented count field correctly" in {
    val h = 10
    val input = 0 until (1 << h)
    val rbt1 = OrderStatisticTree(input: _*)
    val rbt2 = OrderStatisticTree(Random.shuffle(input.toList):_*)

    check(rbt1)(rbt => count(rbt) == rbt.count)
    check(rbt2)(rbt => count(rbt) == rbt.count)

  }

  it should "look up the kth smallest correctly" in {
    val h = 10
    val input = 0 until (1 << h)
    val rbt1 = OrderStatisticTree(input: _*)
    val rbt2 = OrderStatisticTree(Random.shuffle(input.toList):_*)

    for (i <- 0 until rbt1.count) {
      val x = OrderStatisticTree.lookup(rbt1, i)
      //logger.debug(s"k: $i, element: $x")
      x should be (i)
    }

    for (i <- 0 until rbt2.count) {
      val x = OrderStatisticTree.lookup(rbt1, i)
      //logger.debug(s"k: $i, element: $x")
      x should be (i)
    }

  }

}

object OrderStatisticTreeSpec {
  implicit def ints2longs(elems: Seq[Int]): Seq[Long] = {
    elems.map(_.toLong)
  }
}